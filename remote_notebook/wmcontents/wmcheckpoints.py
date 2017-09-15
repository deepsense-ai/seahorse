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


from notebook.services.contents.manager import Checkpoints
import datetime


class WMCheckpoints(Checkpoints):
    def create_checkpoint(self, nb, path):
        return {'id': "stub", 'last_modified': datetime.datetime.now()}

    def restore_checkpoint(self, nb, checkpoint_id, path):
        pass

    def list_checkpoints(self, path):
        return []

    def delete_all_checkpoints(self, path):
        pass
