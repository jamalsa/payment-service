package net.binangkit.payment.biller.pelangi

import scala.math.BigDecimal

import scalaz.concurrent.Task

import argonaut._, Argonaut._

import org.http4s.{Request, Response, UrlForm}
import org.http4s.dsl.{BadRequest, BadRequestSyntax, Ok, OkSyntax}

import net.binangkit.payment.JsonApi
import net.binangkit.payment.api.pln.{Prepaid => BasePrepaid, PrepaidData, InquiryEncoder, PaymentEncoder}

object Prepaid extends BasePrepaid with Api with JsonApi {
  val productId = "80"

  def inquiryHandler(customerNo: String, request: Request): Task[Response] = {

    import Decoder.prepaidInquiryDecoder
    import InquiryEncoder.encoderOf

    val result = sendRequest[PrepaidData](customerNo, "", "2100", "")
    result match {
      case p: PrepaidData => Ok(p)
      case j: Json => BadRequest(j)
      case _ => BadRequest(jsonError("0005", "0005", ""))
    }
  }

  def paymentHandler(customerNo: String, request: Request, trxType: String): Task[Response] = {
    request.decode[UrlForm] {data =>
      val id = data.getFirst("id").getOrElse("")
      val nominal = data.getFirst("nominal").getOrElse("0")

      import Decoder.prepaidPaymentDecoder
      import PaymentEncoder.encoderOf

      val result = sendRequest[PrepaidData](customerNo, nominal, trxType, "")
      result match {
        case p: PrepaidData => Ok(p)
        case j: Json => BadRequest(j)
        case _ => BadRequest(jsonError("0005", "0005", ""))
      }
    }
  }

  def adviceHandler(customerNo: String, request: Request): Task[Response] = paymentHandler(customerNo, request, "2220")
}