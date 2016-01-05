package net.binangkit.payment.biller.pelangi

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date

import scalaz.concurrent.Task

import argonaut._, Argonaut._

import org.http4s.Status.NotFound
import org.http4s.Status.ResponseClass.Successful
import org.http4s._
import org.http4s.dsl._

import scalaj.http.{Http, HttpOptions}

import org.log4s._

import net.binangkit.payment.{Config, JsonApi}

trait ScalajApi extends JsonApi with Config {

  private def logger = getLogger

  def paymentHandler(customerNo: String, request: Request): Task[Response] = 
    paymentHandler(customerNo, request, "2200")

  def paymentHandler(customerNo: String, request: Request, trxType: String): Task[Response]

  val pelangiConfig = config.getConfig("binangkit.biller.pelangi." + env)
  val url = pelangiConfig.getString("url")
  
  val username = pelangiConfig.getString("username")
  val password = pelangiConfig.getString("password")
  val secretKey = pelangiConfig.getString("secretKey")
  val dtFormatter = new SimpleDateFormat("yyyyMMddHHmmss")
  def productId: String


  def md5(raw: String): String = {
    val digest = MessageDigest.getInstance("MD5")
    digest.digest(raw.getBytes).map("%02x".format(_)).mkString
  }

  def generateTrxId(): String = (System.currentTimeMillis % 10000000000L).toString

  def sendRequest[A](
    custNo: String, 
    nominal: String, 
    trxType: String
  )(implicit f: DecodeJson[A]) = {

    val trxDate = dtFormatter.format(new Date)
    val signature = md5(username+password+productId+trxDate+secretKey)
    val authHeader = s"PELANGIREST username=$username&password=$password&signature=$signature"
    
    val data = Seq(
        "trx_date" -> trxDate,
        "trx_type" -> trxType,
        "trx_id" -> "",
        "cust_msisdn" -> "0",
        "cust_account_no" -> custNo,
        "product_id" -> productId,
        "product_nomination" -> nominal,
        "bit11" -> "",
        "bit12" -> "",
        "bit48" -> "",
        "bit62" -> ""
      )

    val request = Http(url).postForm(data)
                  .header("Authorization", authHeader)
                  .option(HttpOptions.allowUnsafeSSL)
                  .option(HttpOptions.connTimeout(20000))
                  .option(HttpOptions.readTimeout(70000))

    logger.info(s"Biller Request to [$url]: $request")

    val response = Task(request.asString)

    response.flatMap{ resp => 
      logger.info(s"Biller Response from [$url]: $resp.code\n\t$resp.body")
      resp.isSuccess match {
        case true => Parse.parseOption(resp.body).map{json =>
          val rc = json.field("data").flatMap(_.field("trx")).flatMap(_.field("rc")) match {
            case Some(rcVal) => {
              if (rcVal.isString) rcVal.stringOr("0005") 
              else if (rcVal.isObject) rcVal.objectValuesOr(List(jString("0005")))(0).stringOr("0005") 
              else "0005"
            }
            case None => "0005"
          }
          rc match {
            case "0000" => {
              val data = json.field("data").flatMap(_.field("trx")).getOrElse(jEmptyObject)
              data.field("msg_type").getOrElse(jEmptyString).stringOrEmpty match {
                case "REVERSAL" => Task.fail(new Throwable("0063;0063;Transaksi Gagal"))
                case _ => data.as[A].value match{
                  case Some(aData) => Task.now(aData)
                  case None => Task.fail(new Throwable("005;005;Error when parsing biller data"))
                }
              }            
            }
            case _ => 
              Task.fail(new Throwable(s"""$rc;$rc;${json.field("data").flatMap(_.field("trx")).flatMap(_.field("desc")).getOrElse(jEmptyString).stringOrEmpty}"""))
          }
        }.getOrElse(Task.fail(new Throwable("005;005;Error when parsing biller data")))

        case false => Task.fail(new Throwable(s"${resp.code};${resp.code};${resp.code}"))
      }
    }.attemptRun
  }
}