# Copyright (c) 2015, CodiLime Inc.

from subprocess import Popen, PIPE

def wait_for_completion(process):
  out = process.stdout.read()
  print out
  print "Popen completed (process errorcode = "+ str(process.wait()) + ")"

class DockerClient(object):
  def __init__(self):
    pass

  def build_docker_image(self, tag, file_name):
    p = Popen(['docker', 'build', '-t', tag, file_name], stdout=PIPE)
    wait_for_completion(p)

  def kill_docker_container(self, name):
    p = Popen(['docker', 'kill', name], stdout=PIPE)
    wait_for_completion(p)

  def remove_docker_container(self, name):
    p = Popen(['docker', 'rm', name], stdout=PIPE)
    wait_for_completion(p)

  def run_docker_container(self, tag, name, *argv):
    p = Popen(['docker', 'run', '--name', name, '-d'] + list(argv) + [tag], stdout=PIPE)
    wait_for_completion(p)

  def delay(self, seconds):
    print "Delay for " + seconds + " seconds"
    import time
    time.sleep(float(seconds))
    print "Delay finished"
