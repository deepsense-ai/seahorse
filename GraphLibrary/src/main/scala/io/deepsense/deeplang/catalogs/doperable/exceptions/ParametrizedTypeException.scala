/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.catalogs.doperable.exceptions

import scala.reflect.runtime.{universe => ru}

case class ParametrizedTypeException(t: ru.Type)
  extends DOperableCatalogException(
    s"Cannot register parametrized type in DHierarchy (Type $t is parametrized)")
