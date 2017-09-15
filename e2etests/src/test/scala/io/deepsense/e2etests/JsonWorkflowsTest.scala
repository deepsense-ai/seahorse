/**
 * Copyright (c) 2016, CodiLime Inc.
 */
package io.deepsense.e2etests

import org.scalatest.{Matchers, WordSpec}

class JsonWorkflowsTest extends WordSpec with Matchers with
  SeahorseIntegrationTestDSL {

  "Running simple worflow loaded from json" should {
    "be correct - all nodes run and completed successfully" in {

      ensureSeahorseIsRunning()
      val filename = getClass.getResource("/02_machinelearning2.json")
      val fileSource = scala.io.Source.fromURL(filename)
      val js = fileSource.mkString
      fileSource.close

      val id = uploadWorkflow(js)

      withExecutor(id) { implicit ctx =>
        launch(id)
        assertAllNodesCompletedSuccessfully(id)
      }
      deleteWorkflow(id)
    }
  }

}
