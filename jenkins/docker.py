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
