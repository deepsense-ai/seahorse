/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang

import io.deepsense.deeplang.doperables.Report
import io.deepsense.deeplang.doperables.dataframe.DataFrameMetadata
import org.json4s.JsonWriter
import spray.json._

/**
 * Objects of classes with this trait can be used in two ways.
 * 1. It can be object on which you can perform DOperations.
 * 2. It can be used to infer knowledge about objects that will be used later in experiment,
 * before it's execution.
 * In second case, only metadata field should be used.
 */
trait DOperable {

  /**
   * Type representing metadata knowledge for DOperable.
   */
  type M <: DOperable.AbstractMetadata

  /**
   * If this instance is used for execution, this field is set to None.
   * If it is used for inference, this contains inferred knowledge about this instance.
   */
  val inferredMetadata: Option[M] = None

  /** Exact metadata for this instance.
    * None means that we don't support metadata for this class in system.
    */
  def metadata: Option[M] = None

  def report: Report

  /**
   * Saves DOperable on HDFS under specified path.
   * Sets url so that it informs where it has been saved.
   */
  def save(executionContext: ExecutionContext)(path: String): Unit

  /**
   * @return path where DOperable is stored, if None is returned then DOperable is not persisted.
   */
  def url: Option[String] = None
}

object DOperable {
  trait AbstractMetadata {
    def accept[T](v: MetadataVisitor[T]): T = v.visit(this)
  }
  trait MetadataVisitor[T] {
    def visit(abstractMetadata: AbstractMetadata): T =
      throw new RuntimeException(
        "Type " + abstractMetadata.getClass.getSimpleName + "not supported by MetadataVisitor")
    def visit(dataFrameMetadata: DataFrameMetadata): T
  }
}
