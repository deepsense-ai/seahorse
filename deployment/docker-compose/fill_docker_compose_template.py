#!/usr/bin/python

from string import Template
import sys

mq_port = sys.argv[1]
wm_port = sys.argv[2]

d = {"GENERATED_MQ_PORT": mq_port, "GENERATED_WM_PORT": wm_port}

filein = open( 'docker-compose.yml.tmpl' )
src = Template( filein.read() )
print src.substitute(d)
