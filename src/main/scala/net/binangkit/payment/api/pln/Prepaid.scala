package net.binangkit.payment.api.pln

import scalaz.concurrent.Task
import org.http4s.{Response, Status}
import org.http4s.dsl.{->, /, GET, POST, Ok, OkSyntax, Root}
import org.http4s.server.HttpService

trait Prepaid {
  
  def service = HttpService {
    case GET -> Root / customerNo => inquiryHandler(customerNo)

    case POST -> Root / customerNo => paymentHandler(customerNo)

    case GET -> Root / customerNo / "check" / trxId => adviceHandler(customerNo)
  }

  def inquiryHandler(customerNo: String): Task[Response]

  def paymentHandler(customerNo: String): Task[Response]

  def adviceHandler(customerNo: String): Task[Response]
}

object Prepaid extends Prepaid {
  def inquiryHandler(customerNo: String): Task[Response] = Ok("Customer no:" + customerNo)

  def paymentHandler(customerNo: String): Task[Response] = Ok("Payment customer no:" + customerNo)

  def adviceHandler(customerNo: String, trxId: String): Task[Response] = 
    Ok("Check customer no:" + customerNo + ", trxId: " + trxId)
}