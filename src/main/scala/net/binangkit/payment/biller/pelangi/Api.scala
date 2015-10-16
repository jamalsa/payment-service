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
import org.http4s.client.blaze.{defaultClient => client}

import net.binangkit.payment.JsonApi

trait Api extends JsonApi {

  def paymentHandler(customerNo: String, request: Request): Task[Response] = 
    paymentHandler(customerNo, request, "2200")

  def paymentHandler(customerNo: String, request: Request, trxType: String): Task[Response]

  val url = uri("http://103.16.138.19:8008/transactions/trx.json")
  //val url = uri("http://127.0.0.1:8181/dummy/pelangi")
  
  val username = "tns14110001"
  val password = "1234"
  val secretKey = "k6a4qeer1piwqfc"
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
          case Some(json) => {
            if (json.isString) json.stringOr("0005") 
            else if (json.isObject) json.objectValuesOr(List(jString("0005")))(0).stringOr("0005") 
            else "0005"
          }
          case None => "0005"
        }
        rc match {
          case "0000" => {
            val data = json.field("data").flatMap(_.field("trx")).getOrElse(jEmptyObject)
            data.as[A].value.getOrElse(jsonError("0005", "0005", ""))
          }
          case _ => 
            jsonError(rc, rc, json.field("data").flatMap(_.field("trx")).flatMap(_.field("desc")).getOrElse(jEmptyString).stringOrEmpty)
        }
      }
      case resp => Task.now(jsonError(resp.status.code.toString, resp.status.reason, ""))
    }.run
  }
}