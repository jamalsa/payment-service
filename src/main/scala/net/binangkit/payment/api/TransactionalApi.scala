package net.binangkit.payment.api

import scalaz.concurrent.Task
import org.http4s.{Request, Response}
import org.http4s.dsl.{->, /, GET, POST, Root}
import org.http4s.server.HttpService

trait TransactionalApi {
  def service = HttpService {
    case request@GET -> Root / customerNo => inquiryHandler(customerNo, request)

    case request@POST -> Root / customerNo => paymentHandler(customerNo, request)

    case request@POST -> Root / customerNo / "check" => adviceHandler(customerNo, request)
  }

  def inquiryHandler(customerNo: String, request: Request): Task[Response]

  def paymentHandler(customerNo: String, request: Request): Task[Response]

  def adviceHandler(customerNo: String, request: Request): Task[Response]
}