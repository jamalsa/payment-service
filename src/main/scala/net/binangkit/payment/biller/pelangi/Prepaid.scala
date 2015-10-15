package net.binangkit.payment.biller.pelangi

import scalaz.concurrent.Task

import argonaut.Json
import argonaut.Argonaut.{jArray, jNumber, jString}

import org.http4s.{Request, Response, UrlForm}
import org.http4s.dsl.{BadRequest, BadRequestSyntax, Ok, OkSyntax}

import net.binangkit.payment.JsonApi
import net.binangkit.payment.api.pln.{Prepaid => BasePrepaid}

object Prepaid extends BasePrepaid with Api with JsonApi {
  val productId = "80"

  def inquiryHandler(customerNo: String, request: Request): Task[Response] = {
    val json = sendRequest(customerNo, "", "2100", "")
    json.hasField("errors") match {
      case false => {
          val result = Json(
            "data" -> Json(
              "id" -> jString(generateUuid),
              "nomor_meter" -> json.fieldOrEmptyString("material_number"),
              "nama" -> json.fieldOrEmptyString("subscriber_name"),
              "tarif" -> json.fieldOrEmptyString("subscriber_segmentation"),
              "daya" -> json.fieldOrZero("power"),
              "stroom_token_unsold" -> jArray(
                  List(
                    json.fieldOrZero("power_purchase_unsold").numberOrZero,
                    json.fieldOrZero("power_purchase_unsold2").numberOrZero
                  ).filter(_ .toDouble > 0).map(jNumber(_))
                )
            )
          )
          Ok(result)
        }
      case true => BadRequest(json)
    }
  }

  def paymentHandler(customerNo: String, request: Request, trxType: String): Task[Response] = {
    request.decode[UrlForm] {data =>
      val id = data.getFirst("id").getOrElse("")
      val nominal = data.getFirst("nominal").getOrElse("0")

      val json = sendRequest(customerNo, nominal, trxType)
      json.hasField("errors") match {
        case false => {
            val result = Json(
              "data" -> Json(
                "id" -> jString(generateUuid),
                "nomor_meter" -> json.fieldOrEmptyString("material_number"),
                "idpel" -> json.fieldOrEmptyString("subscriber_id"),
                "nama" -> json.fieldOrEmptyString("subscriber_name"),
                "tarif" -> json.fieldOrEmptyString("subscriber_segmentation"),
                "daya" -> json.fieldOrZero("power"),
                "no_ref" -> json.fieldOrEmptyString("pln_refno"),
                "rp_bayar" -> json.fieldOrZero("amount"),
                "meterai" -> json.fieldOrZero("meterai"),
                "ppn" -> json.fieldOrZero("ppn"),
                "ppj" -> json.fieldOrZero("ppj"),
                "rp_bayar" -> json.fieldOrZero("amount"),
                "angsuran" -> json.fieldOrZero("angsuran"),
                "rp_stroom_token" -> json.fieldOrZero("power_purchase"),
                "jml_kwh" -> json.fieldOrZero("jml_kwh"),
                "admin_bank" -> json.fieldOrZero("admin_charge"),
                "stroom_token" -> json.fieldOrZero("token"),
                "info_text" -> json.fieldOrZero("info_text")
              )
            )
            Ok(result)
          }
        case true => BadRequest(json)
      }
    }
  }

  def adviceHandler(customerNo: String, request: Request): Task[Response] = paymentHandler(customerNo, request, "2220")
}