package net.binangkit.payment.api

import org.http4s.server.Router

package object pln {
  
  def service = Router(
    "/prepaid" -> Prepaid.service
  )
}