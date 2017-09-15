/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.rest.responses

import io.deepsense.sessionmanager.service.Session

case class ListSessionsResponse(sessions: List[Session])
