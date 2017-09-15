/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.rest

import io.deepsense.commons.buildinfo.BuildInfo
import io.deepsense.commons.utils.Version

object CurrentBuild {
  val version = Version(
    BuildInfo.apiVersionMajor,
    BuildInfo.apiVersionMinor,
    BuildInfo.apiVersionPatch)
}
