# Copyright (c) 2016, CodiLime Inc.

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
