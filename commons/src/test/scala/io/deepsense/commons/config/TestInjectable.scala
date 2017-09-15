/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.commons.config

import javax.inject.{Inject, Named}

object TestInjectable {
  case class Params(
   intValue: Int,
   doubleValue: Double,
   booleanValue: Boolean,
   stringList: Seq[String],
   intList: Seq[Int],
   doubleList: Seq[Double],
   booleanList: Seq[Boolean])
}

class TestInjectable @Inject() (
  @Named("test.int")         intValue: Int,
  @Named("test.double")      doubleValue: Double,
  @Named("test.boolean")     booleanValue: Boolean,
  @Named("test.stringList")  stringList: Seq[String],
  @Named("test.intList")     intList: Seq[Int],
  @Named("test.doubleList")  doubleList: Seq[Double],
  @Named("test.booleanList") booleanList: Seq[Boolean]) {
  val params = TestInjectable.Params(
    intValue, doubleValue, booleanValue, stringList, intList, doubleList, booleanList)
}

