package net.binangkit.payment

import com.typesafe.config.ConfigFactory

trait Config {
  val config = ConfigFactory.load
  val env = config.getString("binangkit.environment")
}