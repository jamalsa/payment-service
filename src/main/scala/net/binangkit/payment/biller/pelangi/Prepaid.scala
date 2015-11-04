package net.binangkit.payment.biller.pelangi

import scala.math.BigDecimal

import scalaz._, scalaz.concurrent.Task

import argonaut._, Argonaut._

import org.http4s.{Request, Response, UrlForm}
import org.http4s.dsl.{BadRequest, BadRequestSyntax, Ok, OkSyntax}

import doobie.imports._

import net.binangkit.payment.{DB, JsonApi}
import net.binangkit.payment.api.pln.{Prepaid => BasePrepaid, PrepaidData, PrepaidInquiryEncoder, PrepaidPaymentEncoder}

object Prepaid extends BasePrepaid with ScalajApi with JsonApi with DB {
  val productId = pelangiConfig.getString("product.prepaid")

  def inquiryHandler(customerNo: String, request: Request): Task[Response] = {

    import Decoder.prepaidInquiryDecoder
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

  def paymentHandler(customerNo: String, request: Request, trxType: String): Task[Response] = {
    request.decode[UrlForm] {data =>
      val id = data.getFirst("id").getOrElse("")
      val nominal = data.getFirst("nominal").getOrElse("0")
      val ppid = data.getFirst("ppid").getOrElse("NOPPID")

      import Decoder.prepaidPaymentDecoder
      import PrepaidPaymentEncoder.encoderOf

      sendRequest[PrepaidData](customerNo, nominal, trxType) match {
        case \/-(p) => p match {
          case d: PrepaidData => {
            updatePaymentToDB(id, d) match {
              case \/-(u) => Ok(d.copy(id=id))
              case -\/(t) => BadRequest(
                jsonError("0005", "0005", "Error when inserting payment data to database: " + t.getMessage)
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

  def updatePaymentToDB(id: String, data: PrepaidData) = {
    val q = sql"""
        update prepaid_transaction
          set flag = 1, idpel = ${data.idpel}, no_ref = ${data.noRef}, rp_bayar =  ${data.rpBayar},
          meterai = ${data.meterai}, ppn = ${data.ppn}, ppj = ${data.ppj}, angsuran = ${data.angsuran}, 
          rp_stroom_token = ${data.rpStroomToken}, jml_kwh = ${data.jmlKwh}, token = ${data.token}, 
          info_text = ${data.infoText}, payment_time=${data.transactionTime}
          where id = $id
      """.update

    val p: Task[Unit] = for {
      _ <- q.run.transact(getTransactor) 
    } yield ()
    p.attemptRun
  }
}