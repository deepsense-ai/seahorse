# Copyright 2015 deepsense.ai (CodiLime, Inc)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


import sys, json
import os
import uuid
import subprocess

EXAMPLES_NAMESPACE_UUID = uuid.UUID('dbfb66e9-a34e-455e-bd9c-157964f03674')

USER_ID = '00000000-0000-0000-0000-000000000000'
USER_NAME = 'seahorse'

DELETE_WF = "DELETE FROM workflows WHERE \"owner_id\" = '{}';".format(USER_ID)
SELECT_WF = "SELECT \"id\" FROM workflows WHERE \"owner_id\" = '{}'".format(USER_ID) # no semicolon!
DELETE_NOTEBOOK = "DELETE FROM notebooks WHERE \"workflow_id\" in ({});".format(SELECT_WF)

INSERT_WF_FORMAT = (
    "INSERT INTO workflows (\"id\", \"created\", \"updated\", \"deleted\", \"owner_id\", \"owner_name\", \"workflow\") "
    "VALUES ('{}', '{}', '{}', False, '{}', '{}', '{}');"
)

INSERT_NOTEBOOK_FORMAT = "INSERT INTO notebooks (\"workflow_id\", \"node_id\", \"notebook\") VALUES ('{}', '{}', '{}');"

def compact_json(content):
    return json.dumps(content, separators=(',', ':')).replace('\'', '\'\'')

def jsonFiles(dir):
    return (file for file in sorted(os.listdir(dir), reverse = True)
            if file.endswith(".json"))

if __name__ == "__main__":
    examplesDir = "examples" if len(sys.argv) < 2 else sys.argv[1]

    print DELETE_NOTEBOOK
    print DELETE_WF

    created = 0
    for file in jsonFiles(examplesDir):
        examplesFile = "{}{}{}".format(examplesDir, os.sep, file)
        with open(examplesFile, "r") as f:
            id = uuid.uuid5(EXAMPLES_NAMESPACE_UUID, file)
            content = json.load(f)
            compact = compact_json(content)
            print INSERT_WF_FORMAT.format(id, created, created, USER_ID, USER_NAME, compact)
            notebooks = content['thirdPartyData']['notebooks']
            for node_id, notebook_json in notebooks.iteritems():
                compact_notebook = compact_json(notebook_json)
                print INSERT_NOTEBOOK_FORMAT.format(id, uuid.UUID(node_id), compact_notebook)

        # We increment 'created' to retain the sorted ordering in the
        # frontend.  The frontend displays workflows with newest on
        # top, hence the reversed flag in the jsonFiles generator
        # expression, ie. we want the workflows that appear earlier in
        # the alphabetically sorted list to have greater creation time.
        created += 1
