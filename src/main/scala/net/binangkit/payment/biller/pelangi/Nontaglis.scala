package net.binangkit.payment.biller.pelangi

import scala.math.BigDecimal

import scalaz._, scalaz.concurrent.Task

import argonaut._, Argonaut._

import org.http4s.{Request, Response, UrlForm}
import org.http4s.dsl.{BadRequest, BadRequestSyntax, Ok, OkSyntax}

import doobie.imports._

import net.binangkit.payment.{DB, JsonApi}
import net.binangkit.payment.api.pln.{Nontaglis => BaseNontaglis, NontaglisData, NontaglisInquiryEncoder, NontaglisPaymentEncoder}

object Nontaglis extends BaseNontaglis with ScalajApi with JsonApi with DB {
  val productId = "105"

  def inquiryHandler(customerNo: String, request: Request): Task[Response] = {

    import Decoder.nontaglisInquiryDecoder
    import NontaglisInquiryEncoder.encoderOf

    sendRequest[NontaglisData](customerNo, "", "2100") match {
      case \/-(p) => p match {
        case d: NontaglisData => {
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
      
      import Decoder.nontaglisPaymentDecoder
      import NontaglisPaymentEncoder.encoderOf

      sendRequest[NontaglisData](customerNo, nominal, trxType) match {
        case \/-(p) => p match {
          case d: NontaglisData => {
            updatePaymentToDB(id, ppid, d) match {
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

  def insertInquiryToDB(data: NontaglisData) = {
    val q = sql"""
        insert into nontaglis_transaction
          (id, inquiry_time, nomor_registrasi, jenis_transaksi, nama, rp_bayar)
          values(${data.id}, now(), ${data.nomorRegistrasi}, ${data.jenisTransaksi}, ${data.nama}, ${data.rpBayar})
      """.update

    val p: Task[Unit] = for {
      xa <- getTransactor
      _ <- q.run.transact(xa) 
      _ <- xa.shutdown
    } yield ()
    p.attemptRun
  }

  
  def updatePaymentToDB(id: String, ppid: String, data: NontaglisData) = {
    val q = sql"""
        update nontaglis_transaction
          set flag = 1, ppid = $ppid, tanggal_registrasi = ${data.tanggalRegistrasi},no_ref = ${data.noRef}, 
          idpel =  ${data.idpel}, biaya_pln = ${data.biayaPln}, admin = ${data.admin}, no_ref = ${data.noRef}, 
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