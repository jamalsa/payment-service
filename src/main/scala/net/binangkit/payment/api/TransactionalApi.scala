package net.binangkit.payment.api

import scalaz.concurrent.Task
import org.http4s.Response
import org.http4s.dsl.{->, /, GET, POST, Root}
import org.http4s.server.HttpService

trait TransactionalApi {
  def service = HttpService {
    case GET -> Root / customerNo => inquiryHandler(customerNo)

    case POST -> Root / customerNo => paymentHandler(customerNo)

    case GET -> Root / customerNo / "check" => adviceHandler(customerNo)
  }

  def inquiryHandler(customerNo: String): Task[Response]

  def paymentHandler(customerNo: String): Task[Response]

  def adviceHandler(customerNo: String): Task[Response]
}