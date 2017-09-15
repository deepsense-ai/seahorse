# Copyright (c) 2015, CodiLime Inc.

import commons
import json
import textwrap
import RestApiClient

class KeystoneClient(RestApiClient.RestApiClient):

  def __init__(self):
    config_section = 'keystone'
    super(KeystoneClient, self).__init__(config_section)
    conf = commons.confGetter(config_section)
    self._insecure = conf('insecure') == 'true'
    if self._insecure:
      self._insecure_token = conf('insecureToken')
    else:
      self._user = conf('user')
      self._tenant = conf('tenant')
      self._password = conf('password')

  def get_token(self):
    if self._insecure:
      return self._insecure_token
    payload = {
      "auth": {
        "tenantName": self._tenant,
        "passwordCredentials": {
            "username": self._user,
            "password": self._password
        }
      }
    }
    response = self._post('tokens', data=json.dumps(payload))
    response_json = response.json()
    if not 'access' in response_json:
      raise AssertionError(textwrap.dedent("""
        Could not get token with given credentials.
        Endpoint: {}
        Request: {}
        Response: {}""").format(self._url(''), payload, response_json))

    return response_json['access']['token']['id']
