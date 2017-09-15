/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.datasource.server

import javax.servlet.ServletContext

import scala.util.control.NonFatal

import org.scalatra.LifeCycle

import io.deepsense.seahorse.datasource.api.DefaultApiImpl

class ScalatraBootstrap extends LifeCycle {

  override def init(context: ServletContext): Unit = {
    try {
      context mount (new DefaultApiImpl(), "/*")
    } catch {
      case NonFatal(e) => e.printStackTrace()
    }
  }

}
