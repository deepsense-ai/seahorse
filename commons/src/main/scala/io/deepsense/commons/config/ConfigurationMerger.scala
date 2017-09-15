/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons.config

import com.typesafe.config.Config
import org.apache.commons.configuration.Configuration
import org.apache.hadoop.conf.{Configuration => HadoopConfiguration}
import org.apache.hadoop.yarn.conf.YarnConfiguration

object ConfigurationMerger {

  def merge[T](config: T, typesafeConfig: Config)(implicit ev: SetableConverter[T]): T = {
    import scala.collection.JavaConversions._
    typesafeConfig.entrySet().foldLeft(ev.from(config)) { case (setable, entry) =>
      setable.set(entry.getKey, typesafeConfig.getString(entry.getKey))
      setable
    }.obj
  }

  trait SetableConverter[A] {
    def from(x: A): Setable[A]
  }

  abstract class Setable[A](val obj: A) {
    def set(key: String, value: String): Unit
  }

  abstract class SetableConverterImpl[A] extends SetableConverter[A]{
    override def from(x: A): Setable[A] = {
      new Setable(x) {
        override def set(key: String, value: String): Unit = {
          setImpl(obj, key, value)
        }
      }
    }

    def setImpl(a: A, key: String, value: String): Unit
  }

  implicit object ApacheConfSetableConverter extends SetableConverterImpl[Configuration] {
    override def setImpl(conf: Configuration, key: String, value: String): Unit = {
      conf.setProperty(key, value)
    }
  }

  implicit object YarnConfSetableConverter extends SetableConverterImpl[YarnConfiguration] {
    override def setImpl(conf: YarnConfiguration, key: String, value: String): Unit = {
      conf.set(key, value)
    }
  }

  implicit object HadoopConfSetableConverter extends SetableConverterImpl[HadoopConfiguration] {
    override def setImpl(conf: HadoopConfiguration, key: String, value: String): Unit = {
      conf.set(key, value)
    }
  }
}
