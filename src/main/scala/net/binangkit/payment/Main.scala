package net.binangkit.payment

import scala.concurrent.duration.Duration

import scalaz.concurrent.Task

import org.http4s.dsl.{/, ->, GET, Ok, OkSyntax, Root}
import org.http4s.server.{HttpService, Router}
import org.http4s.server.blaze.BlazeBuilder

object Main extends App with Config with DB {

  val port = config.getInt(s"binangkit.port.$env")

  def service = Router(
    "" -> rootService,
    "/api" -> api.service,
    "/dummy/pelangi" -> biller.pelangi.dummy.Dummy.service
  )

  def rootService = HttpService {
    case GET -> Root => Ok("Binangkit Payment")
  }

  println(s"Starting server on port $port with environment $env")

  BlazeBuilder
    .bindHttp(port)
    .withIdleTimeout(Duration.Inf)
    .mountService(service, "/")
    .run
    .onShutdown(() => {
      getTransactor.configure(ds => Task.delay(ds.shutdown))
    })
    .awaitShutdown
}