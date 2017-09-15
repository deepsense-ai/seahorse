## How to use vagrant playbook (uses artifactory module)

The `artifactory_example.yml` playbook demonstrates how to use [ansible](https://github.com/DanielRedOak/ansible-mod-artifactory) module that's in `library` directory and hence it's
automatically added by Ansible and visible to playbooks.

Start sbt pointing at the proper Artifactory instance:

    sbt -Dartifactory.url=http://192.168.59.104/artifactory/

Or use `sbt` alone so the Artifactory instance is http://10.10.1.77:8081/artifactory/. Use `show artifactoryUrl` to see
what Artifactory instance you use while in sbt shell.

Publish EntityStorage to Artifactory using `entitystorage/universal:publish` in sbt:

    [deepsense-backend]> entitystorage/universal:publish
    ...
    [info] 	published deepsense-entitystorage to http://192.168.59.104/artifactory/deepsense-backend-snapshot/io/deepsense/deepsense-entitystorage/0.2.0-SNAPSHOT-20150713T123943-38dcf3f-SNAPSHOT/deepsense-entitystorage-0.2.0-SNAPSHOT-20150713T123943-38dcf3f-SNAPSHOT.zip
    [success] Total time: 76 s, completed Jul 13, 2015 2:41:02 PM

Mind the version of EntityStorage, e.g. `0.2.0-SNAPSHOT-20150713T123943-38dcf3f-SNAPSHOT` above. You will use it to run the `artifactory_example.yml` Ansible playbook as `version` variable.

Once EntityStorage is published to Artifactory you can execute Ansible playbook.

Execute the following command to deploy EntityStorage version 0.2.0-SNAPSHOT-20150713T113653-38dcf3f-SNAPSHOT that's
available in Artifactory under http://192.168.59.104:80/artifactory:

    ansible-playbook -i hosts vagrant.yaml -u vagrant \
      --extra-vars "version=0.2.0-SNAPSHOT-20150713T123943-38dcf3f-SNAPSHOT artifactory_host=192.168.59.104 artifactory_port=80"

You should see something along these lines:

    ➜  ansible git:(DS-880-ansible-gerrit-2196) ✗ ansible-playbook -i hosts vagrant.yaml -u vagrant --extra-vars "version=0.2.0-SNAPSHOT-20150713T123943-38dcf3f-SNAPSHOT artifactory_host=192.168.59.104 artifactory_port=80"

    PLAY [Setups machines and components of DeepSense.io] *************************

    GATHERING FACTS ***************************************************************
    ok: [ds-dev-env-master]

    TASK: [Remove installation directory] *****************************************
    changed: [ds-dev-env-master]

    TASK: [Create installation directory] *****************************************
    changed: [ds-dev-env-master]

    TASK: [Download zip package] **************************************************
    changed: [ds-dev-env-master]

    TASK: [Extract zip package] ***************************************************
    changed: [ds-dev-env-master]

    TASK: [Symlink home directory] ************************************************
    changed: [ds-dev-env-master]

    PLAY RECAP ********************************************************************
    ds-dev-env-master          : ok=6    changed=5    unreachable=0    failed=0

## Copyright

Copyright (c) 2015, CodiLime Inc.
