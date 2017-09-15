# Copyright (c) 2015, CodiLime Inc.

import commons
import hdfs
import hdfs.util

class HdfsClient(object):
  def __init__(self):
    conf = commons.confGetter('hdfs')
    host = conf('host')
    port = conf('port')
    user = conf('user')
    self._client = hdfs.InsecureClient('{}:{}'.format(host, port), user=user)

  def download_from_hdfs(self, hdfs_path, local_path):
    print "Downloading file '{}' to local path '{}'".format(hdfs_path, local_path)
    self._client.download(hdfs_path, local_path, overwrite=True)

  def upload_to_hdfs(self, hdfs_path, file_path):
    print "Uploading file '{}' to hdfs path '{}'".format(file_path, hdfs_path)
    self._client.upload(hdfs_path, file_path, overwrite=True)

  def remove_hdfs_path(self, hdfs_path):
    print "Removing hdfs path: '{}'".format(hdfs_path)
    self._client.delete(hdfs_path, recursive=True)

  def check_hdfs_path_exists(self, hdfs_path):
    print "Checking if hdfs path exists: '{}'".format(hdfs_path)
    if self._client.content(hdfs_path, strict=False) == None:
      raise AssertionError("Hdfs path: '{}' does not exist.".format(hdfs_path))

