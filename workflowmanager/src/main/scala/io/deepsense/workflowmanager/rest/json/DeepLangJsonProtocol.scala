/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.rest.json

trait DeepLangJsonProtocol
  extends DOperationCategoryNodeJsonProtocol
  with DOperationDescriptorJsonProtocol
  with HierarchyDescriptorJsonProtocol
  with DOperationEnvelopesJsonProtocol

object DeepLangJsonProtocol extends DeepLangJsonProtocol
