/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graphexecutor.util

import java.io.{ByteArrayInputStream, ObjectInputStream}

/**
 * This class is necessary to deal with objects deserialization when using Spark Context
 * (Spark custom classloaders cannot perform deserialization of complex objects).
 */
class ObjectInputStreamWithCustomClassLoader(
    byteArrayInputStream: ByteArrayInputStream)
  extends ObjectInputStream(byteArrayInputStream) {
  override def resolveClass(classDescriptor: java.io.ObjectStreamClass): Class[_] = {
    try {
      Class.forName(classDescriptor.getName, false, getClass.getClassLoader)
    } catch {
      case ex: ClassNotFoundException => super.resolveClass(classDescriptor)
    }
  }
}
