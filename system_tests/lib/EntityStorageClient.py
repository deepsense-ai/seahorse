# Copyright (c) 2015, CodiLime Inc.

import commons
import httplib
import json
import WithTokenApiClient

class EntityStorageClient(WithTokenApiClient.WithTokenApiClient):

  def __init__(self):
    super(EntityStorageClient, self).__init__('EntityStorage')

  """ Returns un-enveloped report json object """
  def get_entity_report(self, entity_id):
    response = self._get('{}/report'.format(entity_id))
    self._assert_status(response, httplib.OK)
    report_as_string = response.json()['entity']['report']
    return json.loads(report_as_string)
