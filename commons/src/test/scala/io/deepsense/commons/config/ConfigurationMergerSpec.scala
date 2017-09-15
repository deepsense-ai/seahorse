/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons.config

import scala.collection.JavaConversions._

import com.typesafe.config.ConfigFactory
import org.apache.commons.configuration.Configuration
import org.apache.hadoop.conf.{Configuration => HadoopConfiguration}
import org.apache.hadoop.yarn.conf.YarnConfiguration
import org.mockito.ArgumentCaptor
import org.mockito.Mockito._

import io.deepsense.commons.config.ConfigurationMerger.SetableConverter
import io.deepsense.commons.{StandardSpec, UnitTestSupport}

class ConfigurationMergerSpec extends StandardSpec with UnitTestSupport {

  val variable1 = "this.is.a.requested.value"
  val variable2 = "end.this.is.another.value"
  val prefix = "foo"
  val variable3 = "bar"
  val variable4 = "bar2"
  val prefixedVariable3 = prefix + "." + variable3
  val prefixedVariable4 = prefix + "." + variable4
  val value3 = "xyz"
  val value4 = "this is a test"
  val value1 = "1234"
  val value2 = "foobar"
  val inputConfigPrefix = "example"

  val typesafeConfig = ConfigFactory.parseString(
    s"""
       |some.variable = 5
       |$inputConfigPrefix {
       |  $variable1 = $value1
       |  $variable2 = "$value2"
       |
       |  $prefix {
       |    $variable3 = $value3
       |    $variable4 = $value4
       |  }
       |}
    """.stripMargin
  )

  val expectedCalls = List(
    (variable1, value1),
    (variable2, value2),
    (prefixedVariable3, value3),
    (prefixedVariable4, value4)
  )

  val inputConfig = typesafeConfig.getConfig(inputConfigPrefix)
  val expectedCallTimes = times(4)

  "ConfigurationMerger" should {
    "copy configuration from Typesafe Config into Apache Config" in {
      test[Configuration] { case (targetConfig, keysCaptor, valuesCaptor) =>
        verify(targetConfig, expectedCallTimes)
          .setProperty(keysCaptor.capture(), valuesCaptor.capture())
      }
    }
    "copy configuration from Typesafe Config into Hadoop Config" in {
      test[HadoopConfiguration] { case (targetConfig, keysCaptor, valuesCaptor) =>
        verify(targetConfig, expectedCallTimes)
          .set(keysCaptor.capture(), valuesCaptor.capture())
      }
    }
    "copy configuration from Typesafe Config into Yarn Config" in {
      test[YarnConfiguration] { case (targetConfig, keysCaptor, valuesCaptor) =>
        verify(targetConfig, expectedCallTimes)
          .set(keysCaptor.capture(), valuesCaptor.capture())
      }
    }
  }

  private def assertCallsCorrected(
      keysCaptor: ArgumentCaptor[String],
      valuesCaptor: ArgumentCaptor[String]): Unit = {
    val calls = keysCaptor.getAllValues.toList.zip(valuesCaptor.getAllValues.toList)
    calls should contain theSameElementsAs expectedCalls
  }

  private def test[T <: AnyRef](f: (T, ArgumentCaptor[String], ArgumentCaptor[String]) => Unit)
      (implicit ev: SetableConverter[T], manifest: scala.reflect.Manifest[T]): Unit = {
    val targetConfig = mock[T]("targetConf")
    ConfigurationMerger.merge(targetConfig, inputConfig)
    val keysCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
    val valuesCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
    f(targetConfig, keysCaptor, valuesCaptor)
    assertCallsCorrected(keysCaptor, valuesCaptor)
  }
}
