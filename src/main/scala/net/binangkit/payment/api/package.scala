package net.binangkit.payment

import org.http4s.server.Router

package object api {
  
  def service = Router(
    "/pln" -> pln.service
  )
}