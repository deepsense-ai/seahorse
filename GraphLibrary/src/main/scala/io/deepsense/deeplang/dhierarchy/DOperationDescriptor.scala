/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.dhierarchy

import scala.reflect.runtime.universe.Type

/**
 * Represents a registered DOperation and stores its name and i/o port types.
 */
case class DOperationDescriptor(
     name: String,
     description: String,
     category: DOperationCategory,
     inPorts: Seq[Type],
     outPorts: Seq[Type]) {

  override def toString: String = {
    def portsToString(ports: Seq[Type]) = {
      ports.map(DOperationDescriptor.typeToString).mkString(", ")
    }
    s"$name(${portsToString(inPorts)} => ${portsToString(outPorts)})"
  }
}

object DOperationDescriptor {
  /** Helper method that converts scala types to readable strings. */
  private def typeToString(t: Type) : String = {
    t.toString.split(" with ").map(_.split("\\.").toList.last).mkString(" with ")
  }
}
