package net.binangkit.payment.biller.pelangi

import argonaut._, Argonaut._

import net.binangkit.payment.api.pln.PrepaidData

object Decoder {

  implicit def prepaidInquiryDecoder: DecodeJson[PrepaidData] =
    jdecode7L(PrepaidData.apply)(
      "material_number", 
      "subscriber_name", 
      "subscriber_segmentation", 
      "power", 
      "power_purchase_unsold", 
      "power_purchase_unsold2",
      "admin_charge"
    )

      
  implicit def prepaidPaymentDecoder: DecodeJson[PrepaidData] =
    jdecode19L(PrepaidData.apply)(
      "material_number", 
      "subscriber_name", 
      "subscriber_segmentation", 
      "power", 
      "power_purchase_unsold", 
      "power_purchase_unsold2",
      "admin_charge",
      "subscriber_id",
      "switcher_refno",
      "amount",
      "meterai",
      "ppn",
      "ppj",
      "angsuran",
      "power_purchase",
      "jml_kwh",
      "token",
      "info_text",
      "datetime"
    )
}