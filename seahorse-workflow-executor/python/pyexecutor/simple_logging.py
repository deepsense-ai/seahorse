from __future__ import print_function

import sys


def log_debug(s):
    print('[PyExecutor] {}'.format(s))


def log_error(s):
    print('[PyExecutor] {}'.format(s), file=sys.stderr)