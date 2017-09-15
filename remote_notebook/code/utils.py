# Copyright 2016, deepsense.ai
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


import errno
import logging
import os
from sys import stderr
from threading import Thread


def setup_logging(logfile, prefix=''):
    # First, remove any previous configuration
    [logging.root.removeHandler(h) for h in logging.root.handlers[:]]

    directory = os.path.dirname(logfile)
    try:
        os.makedirs(directory)
    except OSError as exc:
        if exc.errno == errno.EEXIST:
            pass
        else:
            raise

    logging.basicConfig(filename=logfile,
                        format='{} %(asctime)s %(name)s::%(funcName)s: %(message)s'.format(prefix),
                        level=logging.DEBUG)


class Logging(object):
    def __init__(self):
        self.logger = logging.getLogger(self.__class__.__name__)
        self.logger.info('Logger initialized for {}'.format(self.__class__.__name__))


def started_daemon_thread(target):
    t = Thread(target=target)
    t.daemon = True
    t.start()
    return t
