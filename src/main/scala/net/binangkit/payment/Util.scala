package net.binangkit.payment

import java.util.UUID


trait Util {

  def generateUuid = UUID.randomUUID.toString
}

object Util extends Util