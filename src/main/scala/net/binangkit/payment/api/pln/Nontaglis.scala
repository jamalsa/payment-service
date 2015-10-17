package net.binangkit.payment.api.pln

import scala.math.BigDecimal

import scalaz.concurrent.Task

import argonaut._, Argonaut._

import org.http4s._
import org.http4s.argonaut._

import net.binangkit.payment.Util
import net.binangkit.payment.api.TransactionalApi
import net.binangkit.payment.biller.pelangi.{Nontaglis => BillerNontaglis}

trait Nontaglis extends TransactionalApi

object Nontaglis extends Nontaglis {
  def inquiryHandler(customerNo: String, request: Request): Task[Response] = 
    BillerNontaglis.inquiryHandler(customerNo, request)

  def paymentHandler(customerNo: String, request: Request): Task[Response] = 
    BillerNontaglis.paymentHandler(customerNo, request)

  def adviceHandler(customerNo: String, request: Request): Task[Response] = 
    BillerNontaglis.adviceHandler(customerNo, request)
}

case class NontaglisData(
  nomorRegistrasi: String, 
  jenisTransaksi: String, 
  nama: String,
  rpBayar: BigDecimal,
  tanggalRegistrasi:  String = "",
  idpel: String = "",
  biayaPln: BigDecimal = 0,
  admin: BigDecimal = 0,
  noRef: String = "",
  transactionTime: String = "",
  infoText: String = "Rincian tagihan dapat diakses di www.pln.co.id atau PLN terdekat",
  flag: Int = 0,  
  id: String = Util.generateUuid
) {
}


object NontaglisData {
  
  def apply(
    nomorRegistrasi: String, 
    jenisTransaksi: String, 
    nama: String,
    rpBayar: BigDecimal
  ): NontaglisData = 
      NontaglisData(
        nomorRegistrasi, 
        jenisTransaksi, 
        nama, 
        rpBayar,
        ""
      )
  
  def apply(
    nomorRegistrasi: String, 
    jenisTransaksi: String, 
    nama: String,
    rpBayar: BigDecimal,
    tanggalRegistrasi:  String,
    idpel: String,
    biayaPln: BigDecimal,
    admin: BigDecimal,
    noRef: String,
    transactionTime: String,
    infoText: String
  ): NontaglisData = 
      this(
        nomorRegistrasi, 
        jenisTransaksi, 
        nama, 
        rpBayar,
        tanggalRegistrasi,
        idpel,
        biayaPln,
        admin,
        noRef,
        transactionTime,
        infoText,
        flag = 1
      )
}


object NontaglisInquiryEncoder {

  implicit def encoder: EncodeJson[NontaglisData] =
      EncodeJson((p: NontaglisData) => 
        ("id" := p.id) ->:
        ("nomor_registrasi" := p.nomorRegistrasi) ->: 
        ("jenis_transaksi" := p.jenisTransaksi) ->: 
        ("nama" := p.nama) ->: 
        ("rp_bayar" := p.rpBayar) ->: 
        jEmptyObject
      )

  implicit def encoderOf = jsonEncoderOf[NontaglisData]
}


object NontaglisPaymentEncoder {

  implicit def encoder: EncodeJson[NontaglisData] =
      EncodeJson((p: NontaglisData) => 
        ("id" := p.id) ->:
        ("nomor_registrasi" := p.nomorRegistrasi) ->: 
        ("jenis_transaksi" := p.jenisTransaksi) ->: 
        ("nama" := p.nama) ->: 
        ("rp_bayar" := p.rpBayar) ->: 
        ("tanggal_registrasi" := p.tanggalRegistrasi) ->:
        ("idpel" := p.idpel) ->:
        ("biaya_pln" := p.biayaPln) ->:
        ("admin" := p.admin) ->: 
        ("no_ref" := p.noRef) ->: 
        ("info_text" := p.infoText) ->:
        ("transaction_time" := p.transactionTime) ->:
        jEmptyObject
      )

  implicit def encoderOf = jsonEncoderOf[NontaglisData]
}
