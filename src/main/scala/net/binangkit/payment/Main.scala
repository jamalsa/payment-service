package net.binangkit

import org.http4s.dsl.{->, GET, Ok, OkSyntax, Root}
import org.http4s.server.{HttpService, Router}
import org.http4s.server.blaze.BlazeBuilder

object Main extends App {

  def service = Router(
    "" -> rootService
  )

  def rootService = HttpService {
    case GET -> Root => Ok("Binangkit Payment")
  }

  BlazeBuilder
    .bindHttp(8181)
    .mountService(service, "/")
    .run
    .awaitShutdown
}