/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.models.workflows

case class WorkflowsList(count: Count, experiments: Seq[Workflow])

case class Count(all: Int, filtered: Int)
