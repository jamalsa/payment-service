package net.binangkit.payment.biller.pelangi

import scala.math.BigDecimal

import scalaz._, scalaz.concurrent.Task

import argonaut._, Argonaut._

import org.http4s.{Request, Response, UrlForm}
import org.http4s.dsl.{BadRequest, BadRequestSyntax, Ok, OkSyntax}

import doobie.imports._

import net.binangkit.payment.{DB, JsonApi}
import net.binangkit.payment.api.pln.{Prepaid => BasePrepaid, PrepaidData, InquiryEncoder, PaymentEncoder}

object Prepaid extends BasePrepaid with Api with JsonApi with DB {
  val productId = "80"

  def inquiryHandler(customerNo: String, request: Request): Task[Response] = {

    import Decoder.prepaidInquiryDecoder
    import InquiryEncoder.encoderOf

    sendRequest[PrepaidData](customerNo, "", "2100") match {
      case \/-(p) => p match {
        case d: PrepaidData => {
          insertInquiryToDB(d) match {
            case \/-(u) => Ok(d)
            case -\/(t) => BadRequest(
              jsonError("0005", "0005", "Error when inserting inquiry data to database: " + t.getMessage)
            )
          }
        }
        case j: Json => BadRequest(j)
        case _ => BadRequest(jsonError("0005", "0005", ""))
      }
      case -\/(t) => BadRequest(jsonError("0005", "0005", t.getMessage))
    }
  }

  def paymentHandler(customerNo: String, request: Request, trxType: String): Task[Response] = {
    request.decode[UrlForm] {data =>
      val id = data.getFirst("id").getOrElse("")
      val nominal = data.getFirst("nominal").getOrElse("0")

      import Decoder.prepaidPaymentDecoder
      import PaymentEncoder.encoderOf

      sendRequest[PrepaidData](customerNo, nominal, trxType) match {
        case \/-(p) => p match {
          case d: PrepaidData => {
            updatePaymentToDB(id, d) match {
              case \/-(u) => Ok(d)
              case -\/(t) => BadRequest(
                jsonError("0005", "0005", "Error when inserting payment data to database: " + t.getMessage)
              )
            }
          }
          case j: Json => BadRequest(j)
          case _ => BadRequest(jsonError("0005", "0005", ""))
        }
        case -\/(t) => BadRequest(jsonError("0005", "0005", t.getMessage))
      }
    }
  }

  def adviceHandler(customerNo: String, request: Request): Task[Response] = paymentHandler(customerNo, request, "2220")

  def insertInquiryToDB(data: PrepaidData) = {
    val q = sql"""
        insert into prepaid_transaction
          (id, inquiry_time, nomor_meter, nama, tarif, daya, admin)
          values(${data.id}, now(), ${data.nomorMeter}, ${data.nama}, ${data.tarif}, ${data.daya}, ${data.admin})
      """.update

    val p: Task[Unit] = for {
      xa <- getTransactor
      _ <- q.run.transact(xa) 
      _ <- xa.shutdown
    } yield ()
    p.attemptRun
  }

  def updatePaymentToDB(id: String, data: PrepaidData) = {
    val q = sql"""
        update prepaid_transaction
          set payment_time=now(), flag = 1, no_ref = ${data.noRef}, rp_bayar =  ${data.rpBayar}, 
          meterai = ${data.meterai}, ppn = ${data.ppn}, ppj = ${data.ppj}, 
          angsuran = ${data.angsuran}, rp_stroom_token = ${data.rpStroomToken}, 
          jml_kwh = ${data.jmlKwh}, token = ${data.token}, info_text = ${data.infoText}
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