# Copyright (c) 2015, CodiLime Inc.

import ConfigParser
import os

config_file_env_var = 'ST_CONFIG'
config_path = os.environ.get(config_file_env_var)
if config_path is None:
  config_path = 'settings.conf'
  print '*WARN* config file not provided, using default path'
  print '*WARN* You can provide path to config path in env var {}'.format(config_file_env_var)
print "*WARN* Using config file '{}'".format(config_path)

config = ConfigParser.ConfigParser()
config.read(config_path)

def confGetter(section):
  def getter(*args, **kwargs):
    return config.get(section, *args, **kwargs)
  return getter
