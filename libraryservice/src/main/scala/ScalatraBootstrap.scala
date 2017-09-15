/**
 * Copyright (c) 2016, CodiLime Inc.
 */

import javax.servlet.ServletContext

import io.deepsense.libraryservice.LibraryServlet
import org.scalatra.LifeCycle

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext): Unit = {
    context mount (new LibraryServlet, "/*")
  }
}
