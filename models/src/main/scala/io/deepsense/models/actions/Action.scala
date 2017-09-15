/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.models.actions

import io.deepsense.graph.Node

/**
 * Workflow Manager's REST API action.
 */
trait Action

case class LaunchAction(nodes: Option[List[Node.Id]]) extends Action
case class AbortAction(nodes: Option[List[Node.Id]]) extends Action
