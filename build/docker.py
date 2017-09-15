# Copyright 2017 deepsense.ai (CodiLime, Inc)
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
import subprocess
import os

cwd = os.path.join(os.path.dirname(os.path.realpath(__file__)), '../..')

def push(image_name):
    docker_push_cmd = "docker push {}".format(image_name)
    print(docker_push_cmd)
    subprocess.check_call(docker_push_cmd, shell=True, cwd=cwd)

def tag(image_name, tag):
    docker_tag_cmd = "docker tag {} {}".format(image_name, tag)
    print(docker_tag_cmd)
    subprocess.check_call(docker_tag_cmd, shell=True, cwd=cwd)

def find_image(query):
    image_cmd = 'docker images -q "{}"'.format(query)
    print(image_cmd)
    image = subprocess.check_output(image_cmd, shell=True, cwd=cwd).strip()
    if not image:
        raise ValueError('There is no image for query "{}"'.format(query))
    return image
