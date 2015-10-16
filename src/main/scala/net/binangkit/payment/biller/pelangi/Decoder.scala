package net.binangkit.payment.biller.pelangi

import argonaut._, Argonaut._

import net.binangkit.payment.api.pln.PrepaidData

object Decoder {

  implicit def prepaidInquiryDecoder: DecodeJson[PrepaidData] =
    jdecode6L(PrepaidData.apply)(
      "material_number", 
      "subscriber_name", 
      "subscriber_segmentation", 
      "power", 
      "power_purchase_unsold", 
      "power_purchase_unsold2"
    )

      
  implicit def prepaidPaymentDecoder: DecodeJson[PrepaidData] =
    jdecode18L(PrepaidData.apply)(
      "material_number", 
      "subscriber_name", 
      "subscriber_segmentation", 
      "power", 
      "power_purchase_unsold", 
      "power_purchase_unsold2",
      "subscriber_id",
      "switcher_refno",
      "amount",
      "meterai",
      "ppn",
      "ppj",
      "angsuran",
      "power_purchase",
      "jml_kwh",
      "admin_charge",
      "token",
      "info_text"
    )
}