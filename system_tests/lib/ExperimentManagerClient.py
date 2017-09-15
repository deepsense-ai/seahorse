# Copyright (c) 2015, CodiLime Inc.

import httplib
import json
import time
import commons
import WithTokenApiClient

class ExperimentManagerClient(WithTokenApiClient.WithTokenApiClient):

  def __init__(self):
    super(ExperimentManagerClient, self).__init__('ExperimentManager')
    conf = commons.confGetter('ExperimentManager')
    self._experiments_prefix = conf('experimentsPrefix')
    self._models_prefix = conf('modelsPrefix')

  def create_experiment_from_file(self, file_path):
    with open(file_path) as f:
      experiment = f.read()
    return self.create_experiment(experiment)

  """ Returns created experiment's id """
  def create_experiment(self, experiment):
    response = self._post_experiment('', data=experiment)
    self._assert_status(response, httplib.CREATED)
    return response.json()['experiment']['id']

  def launch_experiment(self, experiment_id):
    self._post_experiment_action(experiment_id, payload = {'launch': {'targetNodes': []}})

  def abort_experiment(self, experiment_id):
    self._post_experiment_action(experiment_id, payload = {'abort': {'nodes': []}})

  """ Returns deployed model's id """
  def deploy_model(self, model_id):
    response = self._post_model('{}/deploy'.format(model_id), json.dumps({}))
    self._assert_status(response, httplib.OK)
    return response.json()['id']

  def _post_experiment(self, target, *args, **kwargs):
    return self._prefixed_post(self._experiments_prefix, target, *args, **kwargs)

  def _post_model(self, target, *args, **kwargs):
    return self._prefixed_post(self._models_prefix, target, *args, **kwargs)

  def _prefixed_post(self, prefix, target, *args, **kwargs):
    if target == '':
        prefixed_target = prefix
    else:
        prefixed_target = '{}/{}'.format(prefix, target)
    return self._post(prefixed_target, *args, **kwargs)

  def _post_experiment_action(self, experiment_id, payload):
    response = self._post_experiment('{}/action'.format(experiment_id), data=json.dumps(payload))
    self._assert_status(response, httplib.ACCEPTED)
    return response

  """ Returns un-enveloped json description of experiment """
  def get_experiment(self, experiment_id):
    target = '{}/{}'.format(self._experiments_prefix, experiment_id)
    response = self._get(target)
    self._assert_status(response, httplib.OK)
    return response.json()['experiment']

  def delete_experiment(self, experiment_id):
    response = self._delete('{}/{}'.format(self._experiments_prefix, experiment_id))
    self._assert_status(response, httplib.OK)

  def _get_experiment_status(self, experiment):
    return experiment['state']['status']

  """ Waits till experiment finishes and returns its status. """
  def wait_for_experiment_end(self, experiment_id, timeout=None, polling_frequency=0.5):
    if timeout is not None:
      timeout = float(timeout)
    polling_frequency = float(polling_frequency)
    start_time = time.time()
    experiment = None
    status = None
    while True:
      experiment = self.get_experiment(experiment_id)
      status = self._get_experiment_status(experiment)
      if status not in ['QUEUED', 'RUNNING']:
        break
      if timeout is not None and time.time() - start_time > timeout:
        raise AssertionError('Experiment execution timeout ({} s) exceeded'.format(timeout))
      time.sleep(polling_frequency)
    return status

  def experiment_status_should_be(self, experiment, expected_status):
    status = self._get_experiment_status(experiment)
    if status != expected_status:
      raise AssertionError(
          "Experiment status should be '{}' instead of '{}'\nExperiment:\n{}'"\
          .format(expected_status, status, json.dumps(experiment, indent=4)))

  def result_of_node(self, experiment, node, result_index=0):
    return experiment['state']['nodes'][node]['results'][result_index]
