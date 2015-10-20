package net.binangkit.payment.biller.pelangi

import argonaut._, Argonaut._

import net.binangkit.payment.api.pln.{NontaglisData, PostpaidData, PrepaidData}

object Decoder {

  implicit def prepaidInquiryDecoder: DecodeJson[PrepaidData] =
    jdecode5L(PrepaidData.apply)(
      "material_number", 
      "subscriber_name", 
      "subscriber_segmentation", 
      "power", 
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

  implicit def postpaidInquiryDecoder: DecodeJson[PostpaidData] =
    jdecode6L(PostpaidData.apply)(
      "subscriber_id", 
      "subscriber_name", 
      "bill_status", 
      "blth_summary", 
      "amount", 
      "admin_charge"
    )

  
  implicit def postpaidPaymentDecoder: DecodeJson[PostpaidData] =
    jdecode10L(PostpaidData.apply)(
      "subscriber_id", 
      "subscriber_name", 
      "bill_status", 
      "blth_summary", 
      "amount", 
      "admin_charge",
      "switcher_refno",
      "stand_meter_summary",
      "amount", 
      "datetime"
    )

  implicit def nontaglisInquiryDecoder: DecodeJson[NontaglisData] =
    jdecode4L(NontaglisData.apply)(
      "registration_no",
      "transaction_name" ,
      "subscriber_name", 
      "amount"
    )

  
  implicit def nontaglisPaymentDecoder: DecodeJson[NontaglisData] =
    jdecode11L(NontaglisData.apply)(
      "registration_no",
      "transaction_name" ,
      "subscriber_name", 
      "amount",
      "registration_date",
      "subscriber_id",
      "pln_bill",
      "admin_charge",
      "switcher_refno",
      "datetime",
      "info_text"
    )

}