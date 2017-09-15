import httplib
import json
import time
import commons
import RestApiClient

class DeployModelServiceClient(RestApiClient.RestApiClient):

  def __init__(self):
    super(DeployModelServiceClient, self).__init__('DeployModelService')

  def _score_point(self, model_id, point):
    response = self._post(model_id, data=json.dumps(point))
    self._assert_status(response, httplib.OK)
    return response.json()['score']

  def assert_scoring_correct(self, model_id, expected_scoring_path):
    with open(expected_scoring_path) as f:
      content = f.read()
    expected_scoring = json.loads(content)
    tolerance = expected_scoring['tolerance']
    test_cases = expected_scoring['test-cases']
    for test_case in test_cases:
      scored = self._score_point(model_id, test_case['point'])
      expected = test_case['expected']

    if abs((scored - expected))/expected > tolerance:
      raise AssertionError('For point {} got {} instead of {}'\
          .format(test_case['point'], scored, expected))
