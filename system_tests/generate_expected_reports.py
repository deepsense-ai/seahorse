#!/usr/bin/env python

# Copyright (c) 2015, CodiLime Inc.

import sys, getopt
from os import path, listdir, makedirs

sys.path.append("lib")
from json_pattern_utils import create_extracted_pattern_file


def main(argv):
  source_path = "workflow_executor/output"
  destination_path = "workflow_executor/resources"
  try:
    opts, args = getopt.getopt(argv, "hi:o:", [])
  except getopt.GetoptError:
    print __file__ + ' -i <input_dir> -o <output_dir>'
    sys.exit(2)
  for opt, arg in opts:
    if opt == '-h':
      print __file__ + ' -i <input_dir> -o <output_dir>'
      sys.exit()
    elif opt in ("-i"):
      source_path = arg
    elif opt in ("-o"):
      destination_path = arg
  print 'Source directory is ', source_path
  print 'Output directory is ', destination_path

  counter = 0
  suite_dirs = [dir for dir in listdir(source_path)
                if path.isdir(path.join(source_path, dir))]
  for suite_dir in suite_dirs:
    source_suite_path = path.join(source_path, suite_dir)
    destination_suite_path = path.join(destination_path, suite_dir)
    test_dirs = [dir for dir in listdir(source_suite_path)
                 if path.isdir(path.join(source_suite_path, dir))]
    for test_dir in test_dirs:
      source_test_path = path.join(source_suite_path, test_dir)
      destination_test_path = path.join(destination_suite_path, test_dir)
      source_file = path.join(source_test_path, "result.json")
      if path.isfile(source_file):
        output_file = path.join(destination_test_path, "expectedReportPattern.json")
        if not path.exists(destination_test_path):
          makedirs(destination_test_path)
        create_extracted_pattern_file(source_file, output_file)
        print "Report pattern created: " + output_file
        counter += 1
  print str(counter) + " report patterns created"

if __name__ == "__main__":
  main(sys.argv[1:])
