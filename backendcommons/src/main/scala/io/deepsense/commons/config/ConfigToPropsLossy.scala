/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.commons.config

import java.util.Properties

import com.typesafe.config.Config

// http://stackoverflow.com/a/34982538
object ConfigToPropsLossy {
  def apply(config: Config): Properties = {
    import scala.collection.JavaConversions._

    val properties = new Properties()

    val map: Map[String, String] = config.entrySet().map { entry =>
      entry.getKey -> entry.getValue.unwrapped().toString
    } (collection.breakOut)

    properties.putAll(map)
    properties
  }
}
