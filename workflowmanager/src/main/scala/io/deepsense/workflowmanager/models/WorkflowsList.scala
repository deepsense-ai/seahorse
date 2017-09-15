/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.models

import io.deepsense.models.workflows.Workflow

case class WorkflowsList(count: Count, experiments: Seq[Workflow])

case class Count(all: Int, filtered: Int)
