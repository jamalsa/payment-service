package net.binangkit.payment

import argonaut.{DecodeJson, Json}
import argonaut.Argonaut.{jArray, jString}

import org.http4s.argonaut.{jsonEncoder => defaultJsonEncoder, jsonOf}


trait JsonApi extends Util {

  implicit val jsonDecode = DecodeJson.JsonDecodeJson
  implicit val jsonDecoder = jsonOf[Json]
  implicit val jsonEncoder = defaultJsonEncoder

  def jsonError(code: String, title: String, detail: String) = {
    Json("errors" -> 
      Json.array(
        Json(
          "code" -> jString(code),
          "title" -> jString(title),
          "detail" -> jString(detail)
        )
      )
    )
  }
}