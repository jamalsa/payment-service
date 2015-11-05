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
import org.http4s.client._
import org.http4s.client.blaze.SimpleHttp1Client

import net.binangkit.payment.{Config, JsonApi}

trait Api extends JsonApi with Config {

  val client = SimpleHttp1Client(endpointAuthentication = false)

  def paymentHandler(customerNo: String, request: Request): Task[Response] = 
    paymentHandler(customerNo, request, "2200")

  def paymentHandler(customerNo: String, request: Request, trxType: String): Task[Response]

  val pelangiConfig = config.getConfig("binangkit.biller.pelangi." + env)
  val url = Uri.fromString(pelangiConfig.getString("url")).toOption.getOrElse(uri("http://103.16.138.19:8008/transactions/trx.json"))
  
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
    
    val data = UrlForm(
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

    val request = POST(
      url,
      data
    ).putHeaders(Header("Authorization", authHeader))

    client(request).flatMap {
      case Successful(resp) => resp.as[Json].map{json =>
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
              case "REVERSAL" => jsonError("0063", "0063", "Transaksi Gagal")
              case _ => data.as[A].value match{
                case Some(aData) => Task.now(aData)
                case None => Task.fail(new Throwable("005;005;Error when parsing biller data"))
              }
            }            
          }
          case _ => 
            Task.fail(new Throwable(s"""$rc;$rc;${json.field("data").flatMap(_.field("trx")).flatMap(_.field("desc")).getOrElse(jEmptyString).stringOrEmpty}"""))
        }
      }
      case resp => Task.fail(new Throwable(s"${resp.status.code};${resp.status.code};${resp.status.code}"))
    }.attemptRun
  }
}