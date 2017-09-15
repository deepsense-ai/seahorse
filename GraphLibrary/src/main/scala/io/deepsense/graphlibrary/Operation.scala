/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.graphlibrary

import java.util.UUID

/**
 * Mock of DeepLang Operation class.
 */
@SerialVersionUID(101L)
class Operation(id: UUID, val degIn: Int, val degOut: Int) extends Serializable {
  // TODO: this class needs to be changed when DeepLang will become more concrete.
  val total: Int = 10 // just a default value. will be changed.

  def canEqual(other: Any): Boolean = other.isInstanceOf[Operation]

  override def equals(other: Any): Boolean = other match {
    case that: Operation => (that canEqual this) && degIn == that.degIn && degOut == that.degOut
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(degIn, degOut)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}
