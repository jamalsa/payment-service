package net.binangkit.payment.api.pln

import scala.math.BigDecimal

import scalaz.concurrent.Task

import argonaut._, Argonaut._

import org.http4s._
import org.http4s.argonaut._

import net.binangkit.payment.Util
import net.binangkit.payment.api.TransactionalApi
import net.binangkit.payment.biller.pelangi.{Postpaid => BillerPostpaid}

trait Postpaid extends TransactionalApi

object Postpaid extends Postpaid {
  def inquiryHandler(customerNo: String, request: Request): Task[Response] = 
    BillerPostpaid.inquiryHandler(customerNo, request)

  def paymentHandler(customerNo: String, request: Request): Task[Response] = 
    BillerPostpaid.paymentHandler(customerNo, request)

  def adviceHandler(customerNo: String, request: Request): Task[Response] = 
    BillerPostpaid.adviceHandler(customerNo, request)
}

case class PostpaidData(
  idpel: String, 
  nama: String,
  jumlahTagihan: Int,
  blth:  String,
  tagihan: BigDecimal,
  admin: BigDecimal,
  noRef: String = "",
  standMeter: String = "",
  rpBayar: BigDecimal = 0,
  transactionTime: String = "",
  infoText: String = "Rincian tagihan dapat diakses di www.pln.co.id atau PLN terdekat",
  flag: Int = 0,  
  id: String = Util.generateUuid
) {
}


object PostpaidData {
  
  def apply(
    idpel: String, 
    nama: String,
    jumlahTagihan: Int,
    blth:  String,
    rpBayar: BigDecimal,
    admin: BigDecimal
  ): PostpaidData = 
      PostpaidData(
        idpel, 
        nama, 
        jumlahTagihan, 
        blth, 
        rpBayar - admin,
        admin,
        ""
      )
  
  def apply(
    idpel: String, 
    nama: String,
    jumlahTagihan: Int,
    blth:  String,
    rpBayar: BigDecimal,
    admin: BigDecimal,
    noRef: String,
    standMeter: String,
    transactionTime: String
  ): PostpaidData = 
      this(
        idpel, 
        nama, 
        jumlahTagihan, 
        blth, 
        rpBayar - admin,
        admin,
        noRef,
        standMeter,
        rpBayar,
        transactionTime,
        flag = 1
      )
}


object PostpaidInquiryEncoder {

  implicit def encoder: EncodeJson[PostpaidData] =
      EncodeJson((p: PostpaidData) => 
        ("id" := p.id) ->:
        ("idpel" := p.idpel) ->: 
        ("nama" := p.nama) ->: 
        ("jumlah_tagihan" := p.jumlahTagihan) ->: 
        ("blth" := p.blth) ->: 
        ("tagihan" := p.tagihan) ->:        
        ("admin" := p.admin) ->: 
        jEmptyObject
      )

  implicit def encoderOf = jsonEncoderOf[PostpaidData]
}


object PostpaidPaymentEncoder {

  implicit def encoder: EncodeJson[PostpaidData] =
      EncodeJson((p: PostpaidData) => 
        ("id" := p.id) ->:
        ("idpel" := p.idpel) ->: 
        ("nama" := p.nama) ->: 
        ("jumlah_tagihan" := p.jumlahTagihan) ->: 
        ("blth" := p.blth) ->: 
        ("tagihan" := p.tagihan) ->:        
        ("admin" := p.admin) ->: 
        ("no_ref" := p.noRef) ->: 
        ("stand_meter" := p.standMeter) ->: 
        ("rp_bayar" := p.rpBayar) ->: 
        ("info_text" := p.infoText) ->:
        ("transaction_time" := p.transactionTime) ->:
        jEmptyObject
      )

  implicit def encoderOf = jsonEncoderOf[PostpaidData]
}
