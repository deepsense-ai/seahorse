# Copyright (c) 2015, CodiLime Inc.

import commons
from boto.s3.connection import Key, S3Connection

class S3Client(object):
  def __init__(self):
    conf = commons.confGetter('s3')
    bucket = conf('bucket')
    access_key = conf('accessKey')
    secret_key = conf('secretKey')
    self._bucket = S3Connection(access_key, secret_key).get_bucket(bucket)

  def download_from_s3(self, s3_file, local_path):
    print "Downloading file '{}' to local path '{}'".format(s3_file, local_path)
    k = Key(self._bucket)
    k.key = s3_file
    k.get_contents_to_filename(local_path)

  def upload_to_s3(self, s3_file, local_path):
    print "Uploading file '{}' to s3 path '{}'".format(local_path, s3_file)
    k = Key(self._bucket)
    k.key = s3_file
    k.set_contents_from_filename(local_path)

  def remove_from_s3(self, s3_file):
    print "Removing s3 file: '{}'".format(s3_file)
    prefixResultSet = self._bucket.list(prefix=s3_file)
    result = self._bucket.delete_keys([key.name for key in prefixResultSet])

  def check_s3_key_exists(self, s3_file):
    print "Checking if file: '{}' exists".format(s3_file)
    try:
      if self._bucket.get_key(s3_file) == None:
        raise AssertionError("S3 key: '{}' does not exist.".format(s3_file))
    except:
      raise AssertionError("S3 key: '{}' does not exist.".format(s3_file))
