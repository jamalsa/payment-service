package net.binangkit.payment

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date

import scalaz._, scalaz.concurrent.Task

import argonaut._, Argonaut._

import org.http4s._
import org.http4s.dsl._
import org.http4s.server._

import doobie.imports._

import scalaj.http.{Http, HttpOptions}

import org.log4s._

import net.binangkit.payment.api.pln.{PrepaidData, PrepaidInquiryEncoder}

object Benchmark extends JsonApi with Config with DB {

  def service = HttpService {
    case request@GET -> Root / customerNo => benchmark(customerNo, request)
  }

  private def logger = getLogger

  val pelangiConfig = config.getConfig("binangkit.biller.pelangi." + env)
  val url = pelangiConfig.getString("url")
  
  val username = pelangiConfig.getString("username")
  val password = pelangiConfig.getString("password")
  val secretKey = pelangiConfig.getString("secretKey")
  val dtFormatter = new SimpleDateFormat("yyyyMMddHHmmss")
  val productId = "80"

  def md5(raw: String): String = {
    val digest = MessageDigest.getInstance("MD5")
    digest.digest(raw.getBytes).map("%02x".format(_)).mkString
  }

  def benchmark(customerNo: String, request: Request): Task[Response] = {
    import biller.pelangi.Decoder.prepaidInquiryDecoder
    import PrepaidInquiryEncoder.encoderOf

    val ppid = request.params.getOrElse("ppid", "NOPPID")

    sendRequest[PrepaidData](customerNo, "", "2100") match {
      case \/-(p) => p match {
        case d: PrepaidData => {
          insertInquiryToDB(ppid, d) match {
            case \/-(u) => Ok(d)
            case -\/(t) => BadRequest(
              jsonError("0005", "0005", "Error when inserting inquiry data to database: " + t.getMessage)
            )
          }
        }
        case _ => BadRequest(jsonError("0005", "0005", ""))
      }
      case -\/(t) => {
        val msg = t.getMessage.split(";")
        BadRequest(jsonError(msg(0), msg(1), msg(2)))
      }
    }
  }

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
                  .option(HttpOptions.connTimeout(10000))
                  .option(HttpOptions.readTimeout(50000))

    logger.debug(s"Request to $url: $request")

    val response = """{"data":{"trx":{"trx_id":"","stan":"000000040991","datetime":"20151013160414","merchant_code":"6021","bank_code":"4510017","rc":"0000","terminal_id":"0000000000001374","material_number":"14234567891","subscriber_id":"551111111111","pln_refno":"A1D60ECDB792D1711FDE78CC50319681","switcher_refno":"19D06AEE853EF040991F94803BAB40B8","subscriber_name":"BUDIM\"AN\/.,STEAHWAN HD   ","subscriber_segmentation":"R1  ","power":654321,"admin_charge":1600,"distribution_code":"51","service_unit":"51106","service_unit_phone":"900            ","max_kwh_unit":"06000","total_repeat":"2","power_purchase_unsold":"1000000","power_purchase_unsold2":"","saldo":"","bit11":"","bit12":"","bit48":"JTL53L3142345678915511111111110A1D60ECDB792D1711FDE78CC5031968119D06AEE853EF040991F94803BAB40B8BUDIM\"AN\/.,STEAHWAN HD   R1  00065432120000000000","bit62":"5151106900            0600020000100000000010000000"}}}"""

    logger.debug(s"Response from $url: $response")

    val responseTask: Option[Task[A]] = {
      Parse.parseOption(response).map{json =>
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
      }
    }
    responseTask.getOrElse(Task.fail(new Throwable("0005;0005;"))).attemptRun
  }

  def insertInquiryToDB(ppid: String, data: PrepaidData) = {
    val q = sql"""
        insert into prepaid_transaction
          (id, ppid, inquiry_time, nomor_meter, nama, tarif, daya, admin)
          values(${data.id}, $ppid, now(), ${data.nomorMeter}, ${data.nama}, ${data.tarif}, ${data.daya}, ${data.admin})
      """.update

    val p: Task[Unit] = for {
      _ <- q.run.transact(getTransactor) 
    } yield ()
    p.attemptRun
  }
}