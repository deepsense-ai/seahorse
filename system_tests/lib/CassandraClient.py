# Copyright (c) 2015, CodiLime Inc.

import commons
from cassandra.auth import PlainTextAuthProvider
from cassandra.cluster import Cluster
from cassandra.query import tuple_factory


class CassandraClient(object):
  def __init__(self):
    conf = commons.confGetter('cassandra')
    self.host = conf('host')
    self.username = conf('username')
    self.password = conf('password')

  def execute_cql_string(self, cql):
    auth_provider = PlainTextAuthProvider(username=self.username, password=self.password)
    cluster = Cluster([self.host], auth_provider=auth_provider)
    session = cluster.connect()
    rows = session.execute(cql)
    if rows:
      rows.sort()
    return rows
