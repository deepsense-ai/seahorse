/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.rest.responses

import io.deepsense.graph.nodestate.name.NodeStatusName

case class NodeStatusesResponse(nodeStatuses: Map[NodeStatusName, Int])
