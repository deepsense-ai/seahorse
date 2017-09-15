import unittest
import os
import shutil
import sys
sys.path.append('..')

import difflib
import filecmp
from S3Client import S3Client

class S3ClientTest(unittest.TestCase):
  def setUp(self):
    self.client = S3Client()

  def tearDown(self):
    files_to_remove = ['download.txt', 'failure.txt', 'failure2.txt']
    for file_to_remove in files_to_remove:
      if os.path.exists(file_to_remove):
        os.remove(file_to_remove)
    dirs_to_remove = ['parquet']
    for dir_to_remove in dirs_to_remove:
      if os.path.exists(dir_to_remove):
        shutil.rmtree(dir_to_remove)
    keys_to_remove = ['textfile', 'existing', 'file_to_remove']
    for key_to_remove in keys_to_remove:
      self.client.remove_from_s3(key_to_remove)

  def test_upload_and_download_textfile(self):
    key = 'textfile'
    self.client.upload_to_s3(key, 'testfile.txt')
    self.client.download_from_s3(key, 'download.txt')
    diff = difflib.unified_diff(
      open('testfile.txt').readlines(), open('download.txt').readlines())
    self.assertEqual(list(diff), [])

  def test_check_s3_key_exists(self):
    key = 'existing'
    self.client.upload_to_s3(key, 'testfile.txt')
    try:
      self.client.check_s3_key_exists(key)
    except AssertionError:
      self.fail("check_s3_key_exists shouldn't throw on existing key!")

  def test_check_s3_key_not_exists(self):
    with self.assertRaises(Exception):
      self.client.check_s3_key_exists('adswq9eiejjda%')

  def test_simple_remove_key(self):
    key = 'file_to_remove'
    self.client.upload_to_s3(key, 'testfile.txt')
    self.client.remove_from_s3(key)
    with self.assertRaises(Exception):
      self.client.download_from_s3(key, 'failure.txt')

  def test_recursive_remove(self):
    prefix = 'system_tests/recRemoveCatalogue'
    file1key = prefix + '/file.txt'
    file2key = prefix + '/file2.txt'
    self.client.upload_to_s3(file1key, 'testfile.txt')
    self.client.upload_to_s3(file2key, 'testfile.txt')
    self.client.remove_from_s3(prefix)
    with self.assertRaises(Exception):
      self.client.download_from_s3(key, 'failure.txt')
    with self.assertRaises(Exception):
      self.client_download_from_s3(key, 'failure2.txt')

if __name__ == '__main__':
  unittest.main()
