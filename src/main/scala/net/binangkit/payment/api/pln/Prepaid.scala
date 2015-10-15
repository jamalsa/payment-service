package net.binangkit.payment.api.pln

import scalaz.concurrent.Task
import org.http4s.{Request, Response}

import net.binangkit.payment.api.TransactionalApi
import net.binangkit.payment.biller.pelangi.{Prepaid => BillerPrepaid}

trait Prepaid extends TransactionalApi

object Prepaid extends Prepaid {
  def inquiryHandler(customerNo: String, request: Request): Task[Response] = 
    BillerPrepaid.inquiryHandler(customerNo, request)

  def paymentHandler(customerNo: String, request: Request): Task[Response] = 
    BillerPrepaid.paymentHandler(customerNo, request)

  def adviceHandler(customerNo: String, request: Request): Task[Response] = 
    BillerPrepaid.adviceHandler(customerNo, request)
}