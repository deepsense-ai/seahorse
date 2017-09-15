/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.rest.requests

import io.deepsense.commons.models.Id
import io.deepsense.commons.models.ClusterDetails


case class CreateSession(workflowId: Id, cluster: ClusterDetails)
