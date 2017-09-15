## Overview

Directory for Seahorse deployment scripts. Contains deployment code for all Seahorse components.
The scripts are written using [Ansible](http://docs.ansible.com/ansible/index.html).

## Creating Deployment Scripts

For each component deployment, define a role, main playbook for this role and optionally a file
containing variable definitions. For example, to install nginx HTTP server:
* create `nginx` directory in `roles` and define all necessary tasks etc.;
* create `nginx.yml` playbook in main directory (include the `nginx` role inside);
* create `nginx.yml` file in `vars` containing default variables (if necessary).

When creating composite playbooks, which contain deployment of multiple components, use defined
roles and combine them using include statements.

## Executing Deployment Scripts

In order to execute a playbook, define all necessary hosts in inventory file and pass required extra
variables:

    $ ansible-playbook -i hosts -u ubuntu playbook.yml -vv --extra-vars="name=value"


This will execute playbook defined in "playbook.yml" on hosts defined in "hosts" file with some
extra variables set.

## Directory Structure

Directory layout for deployment scripts looks as follows:

```
ansible
│   README.md                  # this file
|
│   hosts                      # main inventory file
│
│   playbook1.yml              # playbooks
│   playbook2.yml
│   ...
│
├───library
│   │   module1.py             # custom modules
│   │   module2.py
│   └   ...
│
├───roles
│   ├───role1                  # hierarchy for role definition
│   │   ├───defaults
│   │   │   └   main.yml       # default role variables
│   │   ├───files
│   │   │   │   file1.conf     # file resources
│   │   │   └   ...
│   │   ├───meta
│   │   │   └   main.yml       # role dependencies
│   │   ├───tasks
│   │   │   └   main.yml       # main role tasks
│   │   └───templates
│   │       │   templ.conf.j2  # template resources
│   │       └   ...
│   │
│   ├───role2
│   └   ...
│
├───tasks
│   │   task1.yml              # common tasks
│   │   task2.yml
│   └   ...
│
└───vars
    │   vars1.yml              # variable definitions
    │   vars2.yml
    └   ...
```

For detailed information see:
[Ansible Best Practices](http://docs.ansible.com/ansible/playbooks_best_practices.html).

## Copyright

Copyright (c) 2016, CodiLime Inc.
