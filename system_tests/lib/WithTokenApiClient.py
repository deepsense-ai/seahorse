# Copyright (c) 2015, CodiLime Inc.

import KeystoneClient
import RestApiClient

class WithTokenApiClient(RestApiClient.RestApiClient):

  def __init__(self, config_section):
    super(WithTokenApiClient, self).__init__(config_section)
    keystone_client = KeystoneClient.KeystoneClient()
    token = keystone_client.get_token()
    self._headers['X-Auth-Token'] = token
