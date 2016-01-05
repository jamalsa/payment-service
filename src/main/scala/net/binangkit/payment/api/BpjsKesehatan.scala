package net.binangkit.payment.api.bpjs

import scala.math.BigDecimal


import scalaz.concurrent.Task
import argonaut._, Argonaut._

import org.http4s._
import org.http4s.argonaut._


import net.binangkit.payment.Util
import net.binangkit.payment.api.TransactionalApi
//import net.binangkit.payment.biller.bubutech.{BpjsKesehatan => Biller}

trait BpjsKesehatan extends TransactionalApi

object BpjsKesehatan extends BpjsKesehatan {
  def inquiryHandler(customerNo: String, request: Request): Task[Response] = ???
      //Biller.inquiryHandler(customerNo, request)

  def paymentHandler(customerNo: String, request: Request): Task[Response] = ???
    //Biller.paymentHandler(customerNo, request)

  def adviceHandler(customerNo: String, request: Request): Task[Response] = ???
    //Biller.adviceHandler(customerNo, request)
}

case class BpjsKesehatanData(
  idpel: String,
  namaPelanggan: String,
  kodeCabang: String,
  namaCabang: String,
  rpBayar: BigDecimal,
  tagihan: BigDecimal,
  jumlahBulan: Int,
  admin: BigDecimal,
  id: String = Util.generateUuid,
  noRef: String = "",
  transactionTime: String = Util.now
)

object BpjsKesehatanInquiryEncoder {

  implicit def encoder: EncodeJson[BpjsKesehatanData] =
      EncodeJson((data: BpjsKesehatanData) => 
        ("id" := data.id) ->:
        ("idpel" := data.idpel) ->:
        ("nama_pelanggan" := data.namaPelanggan) ->:
        ("kode_cabang" := data.kodeCabang) ->:
        ("nama_cabang" := data.namaCabang) ->:
        ("rp_bayar" := data.rpBayar) ->:
        ("rp_tagihan" := data.tagihan) ->:
        ("jumlah_bulan" := data.jumlahBulan) ->:
        ("admin" := data.admin) ->:
        jEmptyObject
      )

  implicit def encoderOf = jsonEncoderOf[BpjsKesehatanData]
}

object BpjsKesehatanPaymentEncoder {

  implicit def encoder: EncodeJson[BpjsKesehatanData] =
      EncodeJson((data: BpjsKesehatanData) => 
        ("id" := data.id) ->:
        ("idpel" := data.idpel) ->:
        ("nama_pelanggan" := data.namaPelanggan) ->:
        ("kode_cabang" := data.kodeCabang) ->:
        ("nama_cabang" := data.namaCabang) ->:
        ("rp_bayar" := data.rpBayar) ->:
        ("rp_tagihan" := data.tagihan) ->:
        ("jumlah_bulan" := data.jumlahBulan) ->:
        ("admin" := data.admin) ->:
        ("no_ref" := data.noRef) ->:
        ("transaction_time" := data.transactionTime) ->:
        jEmptyObject
      )

  implicit def encoderOf = jsonEncoderOf[BpjsKesehatanData]
}