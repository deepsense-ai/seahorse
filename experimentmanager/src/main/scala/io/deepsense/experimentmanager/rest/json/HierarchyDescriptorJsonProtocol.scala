/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.rest.json

import spray.httpx.SprayJsonSupport
import spray.json._

import io.deepsense.deeplang.catalogs.doperable.{ClassDescriptor, HierarchyDescriptor, TraitDescriptor}

trait HierarchyDescriptorJsonProtocol
  extends DefaultJsonProtocol
  with SprayJsonSupport
  with NullOptions {

  implicit val traitDescriptorFormat = jsonFormat2(TraitDescriptor)
  implicit val classDescriptorFormat = jsonFormat3(ClassDescriptor)
  implicit val hierarchyDescriptorFormat = jsonFormat2(HierarchyDescriptor)
}

object HierarchyDescriptorJsonProtocol extends HierarchyDescriptorJsonProtocol
