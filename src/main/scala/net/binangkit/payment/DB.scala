package net.binangkit.payment

import scalaz.concurrent.Task

import doobie.contrib.hikari.hikaritransactor._

trait DB extends Config {
  
  val dbConfig = config.getConfig("binangkit.db." + env)

  def getTransactor = 
    HikariTransactor[Task](
      dbConfig.getString("driver"), 
      dbConfig.getString("url"), 
      dbConfig.getString("user"), 
      dbConfig.getString("password")
    )
}