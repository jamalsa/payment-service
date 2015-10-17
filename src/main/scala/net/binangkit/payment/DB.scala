package net.binangkit.payment

import scalaz.concurrent.Task

import doobie.contrib.hikari.hikaritransactor._

trait DB {
  def getTransactor = 
    HikariTransactor[Task]("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/binangkit_payment", "root", "")
}