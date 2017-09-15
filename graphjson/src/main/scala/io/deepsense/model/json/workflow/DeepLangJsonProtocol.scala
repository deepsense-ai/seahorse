/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.model.json.workflow

trait DeepLangJsonProtocol
  extends DOperationCategoryNodeJsonProtocol
  with DOperationDescriptorJsonProtocol
  with HierarchyDescriptorJsonProtocol
  with DOperationEnvelopesJsonProtocol

object DeepLangJsonProtocol extends DeepLangJsonProtocol
