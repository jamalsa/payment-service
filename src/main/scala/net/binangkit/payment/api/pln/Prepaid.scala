package net.binangkit.payment.api.pln

import scala.math.BigDecimal

import scalaz.concurrent.Task

import argonaut._, Argonaut._

import org.http4s._
import org.http4s.argonaut._

import net.binangkit.payment.Util
import net.binangkit.payment.api.TransactionalApi
import net.binangkit.payment.biller.pelangi.{Prepaid => BillerPrepaid}

trait Prepaid extends TransactionalApi

object Prepaid extends Prepaid {
  def inquiryHandler(customerNo: String, request: Request): Task[Response] = 
    BillerPrepaid.inquiryHandler(customerNo, request)

  def paymentHandler(customerNo: String, request: Request): Task[Response] = 
    BillerPrepaid.paymentHandler(customerNo, request)

  def adviceHandler(customerNo: String, request: Request): Task[Response] = 
    BillerPrepaid.adviceHandler(customerNo, request)
}

case class PrepaidData(
  nomorMeter: String, 
  nama: String,
  tarif: String,
  daya: Int,
  tokenUnsold: List[String],
  admin: BigDecimal,
  idpel: String = "",
  noRef: String = "",
  rpBayar: BigDecimal = 0,
  meterai: BigDecimal = 0,
  ppn: BigDecimal = 0,
  ppj: BigDecimal = 0,
  angsuran: BigDecimal = 0,
  rpStroomToken: BigDecimal = 0,
  jmlKwh: String = "",
  token: String = "",
  infoText: String = "",
  transactionTime: String = "",
  id: String = Util.generateUuid,
  flag: Int = 0
)

object PrepaidData {
  def apply(
    nomorMeter: String,
    nama: String,
    tarif: String,
    daya: Int,
    tokenUnsold1: String,
    tokenUnsold2: String,
    admin: BigDecimal
  ): PrepaidData = 
      this(
        nomorMeter, 
        nama, 
        tarif, 
        daya, 
        List(tokenUnsold1, tokenUnsold2).filter(!_.isEmpty),
        admin
      )

  def apply(
    nomorMeter: String,
    nama: String,
    tarif: String,
    daya: Int,
    tokenUnsold1: String,
    tokenUnsold2: String,
    admin: BigDecimal,
    idpel: String,
    noRef: String,
    rpBayar: BigDecimal,
    meterai: BigDecimal,
    ppn: BigDecimal,
    ppj: BigDecimal,
    angsuran: BigDecimal,
    rpStroomToken: BigDecimal,
    jmlKwh: String,
    token: String,
    infoText: String,
    transactionTime: String
  ): PrepaidData = 
      this(
        nomorMeter, 
        nama, 
        tarif, 
        daya, 
        List(tokenUnsold1, tokenUnsold2).filter(!_.isEmpty),
        admin,
        idpel,
        noRef,
        rpBayar,
        meterai,
        ppn,
        ppj,
        angsuran,
        rpStroomToken,
        jmlKwh,
        token,
        infoText,
        transactionTime
      )
}

object InquiryEncoder {

  implicit def encoder: EncodeJson[PrepaidData] =
      EncodeJson((p: PrepaidData) => 
        ("id" := p.id) ->:
        ("nomor_meter" := p.nomorMeter) ->: 
        ("nama" := p.nama) ->: 
        ("tarif" := p.tarif) ->: 
        ("daya" := p.daya) ->: 
        ("token_unsold" := p.tokenUnsold) ->:        
        ("admin" := p.admin) ->: 
        jEmptyObject
      )

  implicit def encoderOf = jsonEncoderOf[PrepaidData]
}

object PaymentEncoder {

  implicit def encoder: EncodeJson[PrepaidData] =
      EncodeJson((p: PrepaidData) => 
        ("id" := p.id) ->:
        ("nomor_meter" := p.nomorMeter) ->: 
        ("idpel" := p.idpel) ->: 
        ("nama" := p.nama) ->: 
        ("tarif" := p.tarif) ->: 
        ("daya" := p.daya) ->: 
        ("token_unsold" := p.tokenUnsold) ->:         
        ("admin" := p.admin) ->: 
        ("no_ref" := p.noRef) ->: 
        ("rp_bayar" := p.rpBayar) ->: 
        ("meterai" := p.meterai) ->: 
        ("ppn" := p.ppn) ->: 
        ("ppj" := p.ppj) ->: 
        ("angsuran" := p.angsuran) ->: 
        ("rp_stroom_token" := p.rpStroomToken) ->: 
        ("jml_kwh" := p.jmlKwh) ->:
        ("token" := p.token) ->:  
        ("info_text" := p.infoText) ->:
        ("transaction_time" := p.transactionTime) ->:
        jEmptyObject
      )

  implicit def encoderOf = jsonEncoderOf[PrepaidData]
}