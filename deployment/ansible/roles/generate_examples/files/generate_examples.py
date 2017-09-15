# Copyright (c) 2015, CodiLime Inc.

import sys, json
import os
import uuid
import time

INSERT_FORMAT = "INSERT INTO workflows (id, created, updated, deleted, workflow) VALUES ('{}', '{}', '{}', False, '{}');"

created = int(time.time() * 1000)

for file in sorted(os.listdir("examples")):
  if file.endswith(".json"):
    with open("examples/" + file, "r") as f:
      id = uuid.uuid4()
      content = json.load(f)
      compact = json.dumps(content, separators=(',', ':')).replace('\'', '\'\'')
      print INSERT_FORMAT.format(id, created, created, compact)
