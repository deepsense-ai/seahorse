/**
  * Copyright (c) 2016, CodiLime Inc.
  */
package ai.deepsense.deeplang.catalogs

import scala.reflect.runtime.{universe => ru}

import ai.deepsense.deeplang.DOperation.Id
import ai.deepsense.deeplang.{DOperation, DOperation0To1, DOperationCategories, ExecutionContext}
import ai.deepsense.deeplang.catalogs.spi.{CatalogRegistrant, CatalogRegistrar}
import ai.deepsense.deeplang.params.Param
import ai.deepsense.graph.DClassesForDOperations.A1


object SpiLoadedOperation {
  val spiLoadedOperationUuid = "adf440aa-d3eb-4cb9-bf17-bb7fc1d34a0b"
  val spiLoadedOperationId = DOperation.Id.fromString(SpiLoadedOperation.spiLoadedOperationUuid)
}
class SpiLoadedOperation extends DOperation0To1[A1] {
  override protected def execute()(context: ExecutionContext): A1 = ???

  @transient
  override lazy val tTagTO_0: ru.TypeTag[A1] = ru.typeTag[A1]

  override val id: Id = SpiLoadedOperation.spiLoadedOperationUuid
  override val name: String = "SpiLoadedOperation"
  override val description: String = "SpiLoadedOperation"

  override val specificParams: Array[Param[_]] = Array.empty
}

class TestSPIRegistration extends CatalogRegistrant {
  override def register(registrar: CatalogRegistrar): Unit = {
    val prios = SortPriority(12345).inSequence(10)
    registrar.registerOperation(DOperationCategories.IO, () => new SpiLoadedOperation(), prios.next)
  }
}


