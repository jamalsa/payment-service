package net.binangkit.payment

import scalaz.concurrent.Task

import com.zaxxer.hikari.HikariDataSource
import doobie.imports._

trait DB extends Config {
  
  val dbConfig = config.getConfig("binangkit.db." + env)

  private val ds = {
    val _ds = new HikariDataSource()
    _ds.setDriverClassName(dbConfig.getString("driver"))
    _ds.setJdbcUrl(dbConfig.getString("url"))
    _ds.setUsername(dbConfig.getString("user"))
    _ds.setPassword(dbConfig.getString("password"))
    _ds
  }


  val getTransactor = DataSourceTransactor[Task](ds)
}