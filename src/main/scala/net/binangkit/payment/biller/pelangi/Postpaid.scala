package net.binangkit.payment.biller.pelangi

import scala.math.BigDecimal

import scalaz._, scalaz.concurrent.Task

import argonaut._, Argonaut._

import org.http4s.{Request, Response, UrlForm}
import org.http4s.dsl.{BadRequest, BadRequestSyntax, Ok, OkSyntax}

import doobie.imports._

import net.binangkit.payment.{DB, JsonApi}
import net.binangkit.payment.api.pln.{Postpaid => BasePostpaid, PostpaidData, PostpaidInquiryEncoder, PostpaidPaymentEncoder}

object Postpaid extends BasePostpaid with ScalajApi with JsonApi with DB {
  val productId = "100"

  def inquiryHandler(customerNo: String, request: Request): Task[Response] = {

    import Decoder.postpaidInquiryDecoder
    import PostpaidInquiryEncoder.encoderOf

    sendRequest[PostpaidData](customerNo, "", "2100") match {
      case \/-(p) => p match {
        case d: PostpaidData => {
          insertInquiryToDB(d) match {
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

  def paymentHandler(customerNo: String, request: Request, trxType: String): Task[Response] = {
    request.decode[UrlForm] {data =>
      val id = data.getFirst("id").getOrElse("")
      val nominal = data.getFirst("nominal").getOrElse("0")      
      val ppid = data.getFirst("ppid").getOrElse("NOPPID")
      
      import Decoder.postpaidPaymentDecoder
      import PostpaidPaymentEncoder.encoderOf

      sendRequest[PostpaidData](customerNo, nominal, trxType) match {
        case \/-(p) => p match {
          case d: PostpaidData => {
            updatePaymentToDB(id, ppid, d) match {
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
  }

  def adviceHandler(customerNo: String, request: Request): Task[Response] = paymentHandler(customerNo, request, "2220")

  def insertInquiryToDB(data: PostpaidData) = {
    val q = sql"""
        insert into postpaid_transaction
          (id, inquiry_time, idpel, nama, jumlah_tagihan, blth, tagihan, admin)
          values(${data.id}, now(), ${data.idpel}, ${data.nama}, ${data.jumlahTagihan}, ${data.blth}, ${data.tagihan}, ${data.admin})
      """.update

    val p: Task[Unit] = for {
      xa <- getTransactor
      _ <- q.run.transact(xa) 
      _ <- xa.shutdown
    } yield ()
    p.attemptRun
  }

  
  def updatePaymentToDB(id: String, ppid: String, data: PostpaidData) = {
    val q = sql"""
        update postpaid_transaction
          set flag = 1, ppid = $ppid, no_ref = ${data.noRef}, rp_bayar =  ${data.rpBayar}, stand_meter = ${data.standMeter},  
          info_text = ${data.infoText}, payment_time=${data.transactionTime}
          where id = $id
      """.update

    val p: Task[Unit] = for {
      xa <- getTransactor
      _ <- q.run.transact(xa) 
      _ <- xa.shutdown
    } yield ()
    p.attemptRun
  }
}