# Copyright (c) 2015, CodiLime Inc.

import unittest
import sys
sys.path.append('..')
from report_file_utils import extract_pattern, PM

class ReportFileUtilsTest(unittest.TestCase):
  def test_simple_value_matching(self):
    simple_values = [1, None, "string", {}, {'a': 'a'}, [], [2]]
    for val in simple_values:
      self.assertEqual(extract_pattern(val, val), val)
    with self.assertRaises(Exception):
      extract_pattern({}, [])
    with self.assertRaises(Exception):
      extract_pattern(1, 2)

  def test_none_match(self):
    simple_values = [1, None, "string", {}, {'a': 'a'}, [], [2]]
    for val in simple_values:
      self.assertEqual(extract_pattern(None, val), None)

  def test_dict_inspection(self):
    pattern = {'key': {'nested_key': {}}}
    source = {'key': {'nested_key': {}, 'ignored_nested_key': {}}, 'ignored_key': {}}
    expected = pattern
    self.assertEqual(extract_pattern(source, pattern), expected)

  def test_list_inspection(self):
    pattern = [{'key': 1}]
    source = [{'key': 1, 'ignored_key': 2}, {'key': 1, 'another_ignored_key': 3}]
    expected = [{'key': 1}, {'key': 1}]
    self.assertEqual(extract_pattern(source, pattern), expected)
    pattern = [[]]
    source = [[]]
    expected = [[]]
    self.assertEqual(extract_pattern(source, pattern), expected)
    pattern = []
    source = [1,2,3]
    expected = []
    with self.assertRaises(Exception):
      self.assertEqual(extract_pattern(source, pattern), expected)
    pattern = [{'key': PM.Match}]
    source = [{'key': 1, 'ignored_key': 2}, {'key': 2, 'ignored_key2': 2}]
    expected = [{'key': 1}, {'key': 2}]
    self.assertEqual(extract_pattern(source, pattern), expected)

  def test_matchers(self):
    pattern = {
      'key': PM.Match,
      'ignore': PM.Ignore,
      'ignore2': PM.Ignore,
      PM.Any: {'a': PM.Match}
    }
    source = {
      'key': 1,
      'ignore': [1,2,3,4],
      'ignore2': {},
      'some_first_key': {'a': 1, 'b': 0},
      'some_second_key': {'a': 2, 'b': 0}
    }
    expected = {
      'key': 1,
      'some_first_key': {'a': 1},
      'some_second_key': {'a': 2}
    }
    self.assertEqual(extract_pattern(source, pattern), expected)

if __name__ == '__main__':
  unittest.main()
