# Copyright (c) 2016, CodiLime Inc.

from sys import stderr
from datetime import datetime
from threading import Thread


def debug(msg):
    print >>stderr, datetime.now(), msg
    with open('/tmp/executing_kernel.log', 'a') as f:
        f.write('[{}] {}\n'.format(datetime.now(), msg))


def started_daemon_thread(target):
    t = Thread(target=target)
    t.daemon = True
    t.start()
    return t
