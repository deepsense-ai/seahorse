
import unittest
import os
import shutil
import sys
sys.path.append('..')

import difflib
import filecmp
from HdfsClient import HdfsClient

class HdfsClientTest(unittest.TestCase):
  def setUp(self):
    self.client = HdfsClient()

  def tearDown(self):
    files_to_remove = ['download.txt', 'failure.txt']
    for file_to_remove in files_to_remove:
      if os.path.exists(file_to_remove):
        os.remove(file_to_remove)
    dirs_to_remove = ['parquet']
    for dir_to_remove in dirs_to_remove:
      if os.path.exists(dir_to_remove):
        shutil.rmtree(dir_to_remove)
    self.client.remove_hdfs_path('/hadoop')

  def test_upload_and_download_textfile(self):
    hadoop_path = '/hadoop/path/testfile.txt'
    self.client.upload_to_hdfs(hadoop_path, 'testfile.txt')
    self.client.download_from_hdfs(hadoop_path, 'download.txt')
    diff = difflib.unified_diff(open('testfile.txt').readlines(), open('download.txt').readlines())
    self.assertEqual(list(diff), [])

  def test_upload_and_download_parquet(self):
    hadoop_path = '/hadoop/path/LocationAttractivenessOutput.csv'
    local_path = 'LocationAttractivenessOutput.csv'
    self.client.upload_to_hdfs(hadoop_path, local_path)
    self.client.download_from_hdfs(hadoop_path, 'parquet')
    self.assertEqual(filecmp.dircmp(local_path, 'parquet').diff_files, [])

  def test_check_hdfs_path_exists(self):
    hadoop_path = '/hadoop/path/existing.txt'
    self.client.upload_to_hdfs(hadoop_path, 'testfile.txt')
    try:
      self.client.check_hdfs_path_exists(hadoop_path)
    except AssertionError:
      self.fail("check_hdfs_path_exists shouldn't throw on existing file!")

  def test_check_hdfs_path_not_exists(self):
    with self.assertRaises(Exception):
      self.client.check_hdfs_path_exists('rajkdas938241asd.jpg')

  def test_remove_hdfs_path(self):
    hadoop_path = '/hadoop/path/removetest.txt'
    self.client.upload_to_hdfs(hadoop_path, 'testfile.txt')
    self.client.remove_hdfs_path('/hadoop')
    with self.assertRaises(Exception):
      self.client.download_from_hdfs(hadoop_path, 'failure.txt')

if __name__ == '__main__':
  unittest.main()
