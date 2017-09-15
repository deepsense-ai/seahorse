/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.inference.exceptions

import io.deepsense.deeplang.exceptions.DeepLangException

case class NameNotUniqueException(
    name: String)
  extends DeepLangException(s"Name '$name' is not unique")
