# GETTING STARTED WITH DEVELOPMENT

```
# Init nested workflow-executor repo
git submodule init
git submodule update

# Setting up gerrit for seahorse-workflow-executor
cd seahorse-workflow-executor
git remote remove origin
git remote add origin ssh://USER.NAME@gerrit.codilime.com:29418/ds-workflow_executor  # (copy link from gerrit)
cd ..
cp .git/hooks/commit-msg .git/modules/seahorse-workflow-executor/hooks/commit-msg

# Setting up gerrit for seahorse-sdk-example
cd seahorse-sdk-example
git remote remove origin
git remote add origin ssh://USER.NAME@gerrit.codilime.com:29418/seahorse-sdk-example  # (copy link from gerrit)
cd ..
cp .git/hooks/commit-msg .git/modules/seahorse-sdk-example/hooks/commit-msg
```

## BASH COMPLETION FOR PYTHON SCRIPTS

Some of our Python scripts used by devs support bash autocompletion using argcomplete.

```
pip install argcomplete
activate-global-python-argcomplete --user
```

See [this](http://argcomplete.readthedocs.io/en/latest/#activating-global-completion) for global completion support.

### Mac OS
Note, that bash 4.2 is required.
[Installation instruction for Mac users](http://argcomplete.readthedocs.io/en/latest/#global-completion)

After the bash upgrade, you may have to rename `.bash_profile` to `.bashrc`. And maybe add `/usr/local/bin` to $PATH.
Also, check if you're actually running the new bash with `echo $BASH_VERSION` - your terminal might still be using the old one.


# START SEAHORSE LATEST DEV VERSION

### Ensure that you have [access to Docker Hub](https://codilime.atlassian.net/wiki/display/DM/Private+docker-hub)

### Run
```
(cd deployment/docker-compose; ./docker-compose-latest up)
```

### Go to [http://localhost:33321](http://localhost:33321)
