/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.dhierarchy.exceptions

import scala.reflect.runtime.{universe => ru}

class ParametrizedTypeException(val t: ru.Type)
  extends DHierarchyException(
    s"Cannot register parametrized type in DHierarchy (Type $t is parametrized)")
