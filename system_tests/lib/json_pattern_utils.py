# Copyright (c) 2015, CodiLime Inc.

import json

"""
 Expected report pattern generator.
 Single element list is treated as pattern for all elements from source.
 Dict/List inspection is optional when source value is null or is not accessible.
 Consider {"error": {"details": PM.Ignore, "code": PM.Match ...}} matching {"error": null} is fine.
 Matching with {"error": []} will fail
"""


class PM:
  class Ignore: pass  # As dict value: key and value is ignored

  class Match: pass  # As dict value: entire json structure is copied as expected pattern if exists

  class Any: pass  # As dict key: keys not ignored and not directly specified


MAX_RELATIVE_ERROR = 0.5
MAX_ABSOLUTE_ERROR = 1

REPORT_PATTERN = {
  'workflow': PM.Ignore,
  'thirdPartyData': PM.Ignore,
  'id': PM.Ignore,
  'metadata': PM.Ignore,
  'executionReport': {
    'resultEntities': {
      PM.Any: {
        'className': PM.Ignore,
        'report': {
          'name': PM.Ignore,
          'tables': [
            {
              'description': PM.Ignore,
              PM.Any: PM.Match
            }
          ],
          'distributions': {
            PM.Any: {
              'description': PM.Ignore,
              'buckets': PM.Ignore,
              'counts': PM.Ignore,
              'statistics': {
                'min': PM.Match,
                'max': PM.Match,
                'mean': PM.Match
              },
              PM.Any: PM.Match
            }
          },
          PM.Any: PM.Match
        }
      }
    },
    'error': {
      'code': PM.Match,
      'title': PM.Match
    },
    'nodes': {
      PM.Any: {
        'ended': PM.Ignore,
        'started': PM.Ignore,
        'error': {
          'code': PM.Match
        },
        'status': PM.Match,
        PM.Any: PM.Match
      }
    }
  }
}


def create_extracted_pattern_file(source_filename, output_filename, pattern=REPORT_PATTERN):
  source = load_json(source_filename)
  extracted = extract_pattern(source, pattern)
  save_json(output_filename, extracted)


def extract_pattern(source, pattern=REPORT_PATTERN):
  if type(pattern) != type(source):
    if source is None:
      return None
    elif pattern == PM.Match:
      return source
    else:
      raise Exception(
        "Pattern: " + str(pattern) + " and " + "source: " + str(source) + " types don't match")
  common_type = type(pattern)
  if common_type == dict:
    return extract_dict_pattern(source, pattern)
  elif common_type == list:
    return extract_list_pattern(source, pattern)
  else:
    if source != pattern:
      raise Exception("Pattern doesn't match: " + str(pattern) + " != " + str(source))
    else:
      return source


def extract_dict_pattern(source, pattern):
  ignored_keys = set([key for (key, val) in pattern.iteritems()
                      if val == PM.Ignore and type(key) == str])
  specified_keys = set([key for (key, val) in pattern.iteritems()
                        if val != PM.Ignore and type(key) == str])
  ignored_specified_intersection = ignored_keys & specified_keys
  if len(ignored_specified_intersection) > 0:
    raise Exception("Ignored keys: " + str(ignored_specified_intersection) + " are specified")

  specified = {key: pattern[key] for key in specified_keys}
  to_resolve_patterns = specified.copy()

  if PM.Any in pattern:
    any_keys = set(source.keys()) - (ignored_keys | specified_keys)
    any_key = {key: pattern[PM.Any] for key in any_keys}
    to_resolve_patterns.update(any_key)

  result = {}
  for (k, v) in to_resolve_patterns.iteritems():
    if k in source:
      result[k] = extract_pattern(source[k], v)
  return result


def extract_list_pattern(source, pattern):
  if pattern == []:
    if source != []:
      raise Exception("List pattern " + str(pattern) + " can't be applied to " + str(source))
    else:
      return source
  if len(pattern) > 1:
    raise Exception(
      "List pattern can hold only one pattern to be applied for all elements " + str(pattern))

  return [extract_pattern(el, pattern[0]) for el in source]


def match_report(expected_report_path, result_report_path):
  json_result = load_json(result_report_path)
  json_pattern = load_json(expected_report_path)
  mapped_json_pattern = map_pattern_result_entities_ids(json_pattern, json_result)
  try:
    check_json_containment(mapped_json_pattern, json_result)
  except Exception as e:
    raise AssertionError('Actual report does not match pattern. ' + str(e))


def is_contained(contained, container):
  try:
    check_json_containment(contained, container)
    return True
  except Exception:
    return False


def check_json_containment(contained, container):
  """ `contained` is contained by `container` if all keys from `contained`        """
  """ are present in `container` and their values from `contained` are contained  """
  """ in corresponding values from `container`. For strings containment is        """
  """ to equality. For numerics, relative error needs to be smaller than          """
  """ MAX_RELATIVE_ERROR                                                          """
  if type(contained) != type(container):
    raise Exception(str(contained) + " and " + str(container) + " don't have the same type")
  elif type(contained) == dict:
    contained_key_set = set(contained.keys())
    container_key_set = set(container.keys())
    if not contained_key_set.issubset(container_key_set):
      raise Exception("{ " + ", ".join(container.keys()) +
                      " } does not have all keys from { " + ", ".join(contained.keys()) + " }")
    for key in contained.keys():
      check_json_containment(contained[key], container[key])
  elif type(contained) == list and type(container) == list:
    if len(contained) != len(container):
      raise Exception(
        "Lists length doesn't match " + str(len(contained)) + "!=" + str(len(container))
        + " " + str(contained) + " != " + str(container))
    zipped = zip(contained, container)
    for (contained_value, container_value) in zipped:
      check_json_containment(contained_value, container_value)
  else:
    if contained != container:
      if (isinstance(contained, float) or isinstance(contained, int)) and \
        (isinstance(container, float) or isinstance(container, int)):
        if not are_in_error_margin(float(container), float(contained)):
          raise Exception(
            "Number is not within margin error " + str(contained) + " !~ " + str(container) + ")")
      else:
        if (isinstance(contained, unicode) or isinstance(contained, str)) and \
          (isinstance(container, unicode) or isinstance(container, str)):
          try:
            if not are_in_error_margin(float(container), float(contained)):
              raise Exception("String number is not within margin error " +
                              str(contained) + " !~ " + str(container) + ")")
          except ValueError:
            raise Exception(str(contained) + " != " + str(container))
        else:
          raise Exception(
            str(type(contained)) + str(contained) + " != " + str(type(contained)) + " " + str(
              container))


def are_in_error_margin(a, b):
  if absolute_error(a, b) > MAX_ABSOLUTE_ERROR:
    if relative_error(a, b) > MAX_RELATIVE_ERROR:
      return False
  return True


def absolute_error(a, b):
  return abs(a - b)


def relative_error(a, b):
  return abs(a - b) / abs(float(b))


def map_pattern_result_entities_ids(json_pattern, json_result):
  mapped_json_pattern = json_pattern.copy()
  for node_id in json_pattern['executionReport']['nodes'].keys():
    try:
      result_entities = json_result['executionReport']['nodes'][node_id]['results']
      expected_entities = json_pattern['executionReport']['nodes'][node_id]['results']
    except KeyError:
      raise AssertionError('Missing node ' + node_id + 'in execution report.')
    if type(result_entities) == list and type(expected_entities) == list:
      if len(result_entities) != len(expected_entities):
        raise AssertionError('Actual report contains unexpected number of entities.')
      else:
        mapped_json_pattern['executionReport']['nodes'][node_id]['results'] = result_entities
        zipped_entities_ids = zip(expected_entities, result_entities)
        for (expected_entity_id, result_entity_id) in zipped_entities_ids:
          entity_pattern = json_pattern['executionReport']['resultEntities'][expected_entity_id]
          del mapped_json_pattern['executionReport']['resultEntities'][expected_entity_id]
          mapped_json_pattern['executionReport']['resultEntities'][
            result_entity_id] = entity_pattern
    else:
      if not ((result_entities is None or result_entities == []) and
                (expected_entities is None or expected_entities == [])):
        raise AssertionError(
          "Actual report contains unexpected entities value: " + str(result_entities) +
          " != " + str(expected_entities))
  return mapped_json_pattern


def load_json(filename):
  json_file = open(filename, 'r')
  json_content = json.loads(json_file.read())
  json_file.close()
  return json_content


def save_json(filename, data):
  with open(filename, 'w') as outfile:
    json.dump(data, outfile, indent=2)
