#!/usr/bin/env bash

# usage: ./replace_envs.sh filename
# replaces all occurrences of ${X} with value of environmental variable X. Note the curly braces.

# source: http://stackoverflow.com/a/5274448
perl -p -e 's/\$\{([^}]+)\}/defined $ENV{$1} ? $ENV{$1} : $&/eg; s/\$\{([^}]+)\}//eg' $1 | sponge $1
