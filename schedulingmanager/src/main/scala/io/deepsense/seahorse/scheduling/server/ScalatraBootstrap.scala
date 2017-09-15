/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.scheduling.server

import javax.servlet.ServletContext

import scala.util.control.NonFatal

import org.scalatra.LifeCycle

import io.deepsense.seahorse.scheduling.api.DefaultApiImpl

class ScalatraBootstrap extends LifeCycle {

  override def init(context: ServletContext): Unit = {
    try {
      context mount (new DefaultApiImpl(), "/*")
    } catch {
      case NonFatal(e) => e.printStackTrace()
    }
  }

}
