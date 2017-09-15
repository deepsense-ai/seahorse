/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.commons.serialization

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}

trait Serialization {

  def deserialize[T](bytes: Array[Byte]): T = {
    val bufferIn = new ByteArrayInputStream(bytes)
    val streamIn = new ObjectInputStream(bufferIn)
    try {
      streamIn.readObject().asInstanceOf[T]
    } finally {
      streamIn.close()
    }
  }

  def serialize[T](objectToSerialize: T): Array[Byte] = {
    val byteArrayOutputStream: ByteArrayOutputStream = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(byteArrayOutputStream)
    try {
      oos.writeObject(objectToSerialize)
      oos.flush()
      byteArrayOutputStream.toByteArray
    } finally {
      oos.close()
    }
  }

  def serializeDeserialize[T](obj: T): T = deserialize[T](serialize[T](obj))

  object Serialization extends Serialization
}
