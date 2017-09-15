# Copyright (c) 2015, CodiLime Inc.

import sys, json
import os
import uuid
import time

INSERT_WF_FORMAT = "INSERT INTO workflows (id, created, updated, deleted, workflow) VALUES ('{}', '{}', '{}', False, '{}');"
INSERT_NOTEBOOK_FORMAT = "INSERT INTO notebooks (workflow_id, node_id, notebook) VALUES ('{}', '{}', '{}');"

def compact_json(content):
  return json.dumps(content, separators=(',', ':')).replace('\'', '\'\'')

created = int(time.time() * 1000)

for file in sorted(os.listdir("examples")):
  if file.endswith(".json"):
    with open("examples/" + file, "r") as f:
      id = uuid.uuid4()
      content = json.load(f)
      compact = compact_json(content)
      print INSERT_WF_FORMAT.format(id, created, created, compact)
      notebooks = content['thirdPartyData']['notebooks']
      for node_id, notebook_json in notebooks.iteritems():
        compact_notebook = compact_json(notebook_json)
        print INSERT_NOTEBOOK_FORMAT.format(id, uuid.UUID(node_id), compact_notebook)
      created -= 1

