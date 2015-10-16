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
  noRef: String = "",
  rpBayar: BigDecimal = 0,
  meterai: BigDecimal = 0,
  ppn: BigDecimal = 0,
  ppj: BigDecimal = 0,
  angsuran: BigDecimal = 0,
  rpStroomToken: BigDecimal = 0,
  jmlKwh: Double = 0,
  admin: BigDecimal = 0,
  token: String = "",
  infoText: String = "",
  id: String = Util.generateUuid,
  extra: String = ""
)

object PrepaidData {
  def apply(
    nomorMeter: String,
    nama: String,
    tarif: String,
    daya: Int,
    tokenUnsold1: String,
    tokenUnsold2: String
  ): PrepaidData = 
      this(
        nomorMeter, 
        nama, 
        tarif, 
        daya, 
        List(tokenUnsold1, tokenUnsold2)
      )

  def apply(
    nomorMeter: String,
    nama: String,
    tarif: String,
    daya: Int,
    tokenUnsold1: String,
    tokenUnsold2: String,
    noRef: String,
    rpBayar: BigDecimal,
    meterai: BigDecimal,
    ppn: BigDecimal,
    ppj: BigDecimal,
    angsuran: BigDecimal,
    rpStroomToken: BigDecimal,
    jmlKwh: Double,
    admin: BigDecimal,
    token: String,
    infoText: String
  ): PrepaidData = 
      this(
        nomorMeter, 
        nama, 
        tarif, 
        daya, 
        List(tokenUnsold1, tokenUnsold2),
        noRef,
        rpBayar,
        meterai,
        ppn,
        ppj,
        angsuran,
        rpStroomToken,
        jmlKwh,
        admin,
        token
      )
}

object PrepaidDataEncoder {

  implicit def inquiryEncoder: EncodeJson[PrepaidData] =
      EncodeJson((p: PrepaidData) => 
        ("material_number" := p.nomorMeter) ->: ("subscriber_name" := p.nama) ->: ("subscriber_segmentation" := p.tarif) ->: ("power" := p.daya) ->: ("token_unsold" := p.tokenUnsold) ->: jEmptyObject)

  implicit def resultInquiryEncoder = jsonEncoderOf[PrepaidData]
}