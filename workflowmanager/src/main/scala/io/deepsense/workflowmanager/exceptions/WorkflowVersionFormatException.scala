/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.exceptions

case class WorkflowVersionFormatException(stringVersion: String)
  extends WorkflowVersionException(
    "Workflow's version has a wrong format. ",
    "Workflow's version has a wrong format. " +
      s"Got '$stringVersion' but expected X.Y.Z " +
      "where X, Y, Z are positive integers")
