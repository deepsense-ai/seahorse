/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Rafal Hryciuk
 */

package io.deepsense.experimentmanager

import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.dataframe.DataFrame

/**
 * Object used to register all wanted DOperables in a catalog.
 */
object CatalogRecorder {

  def registerDOperable(catalog: DOperableCatalog) = {
    catalog.registerDOperable[DataFrame]()
  }

}
