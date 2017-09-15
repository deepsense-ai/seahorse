# Copyright (c) 2015, CodiLime Inc.

import commons
import requests

class RestApiClient(object):

  _timeout = 10

  def __init__(self, config_section):
    conf = commons.confGetter(config_section)
    self._host = conf('host')
    self._port = conf('port')
    self._api_prefix = conf('apiPrefix')
    self._headers = {
      'Content-Type': 'application/json'
    }

  def _assert_status(self, response, expected_status):
    actual_status = response.status_code
    if int(actual_status) != expected_status:
      raise AssertionError('Response status: {}, expected: {}.\nResponse:\n{}'\
          .format(actual_status, expected_status, response.content))

  def _post(self, target, *args, **kwargs):
    url = self._url(target)
    print '*INFO* POST {}'.format(url)
    return requests.post(
        url, headers=self._headers, timeout=RestApiClient._timeout,
        *args, **kwargs)

  def _get(self, target, *args, **kwargs):
    url = self._url(target)
    print '*INFO* GET {}'.format(url)
    return requests.get(
         url, headers=self._headers, timeout=RestApiClient._timeout,
        *args, **kwargs)

  def _delete(self, target, *args, **kwargs):
    url = self._url(target)
    print '*INFO* DELETE {}'.format(url)
    return requests.delete(
         url, headers=self._headers, timeout=RestApiClient._timeout,
        *args, **kwargs)

  def _url(self, target):
    return '{}:{}/{}/{}'.format(self._host, self._port, self._api_prefix, target)
