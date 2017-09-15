# GETTING STARTED WITH DEVELOPMENT

```
# Init nested workflow-executor repo
git submodule init
git submodule update

# Compile classes from workflow-executor that are needed to compile deepsense-backend
sbt publishWeClasses

# Setting up gerrit
cd seahorse-workflow-executor
git remote remove origin
git remote add origin ssh://USER.NAME@gerrit.codilime.com:29418/ds-workflow_executor  # (copy link from gerrit)
cd ..
cp .git/hooks/commit-msg .git/modules/seahorse-workflow-executor/hooks/commit-msg
```

# START SEAHORSE LATEST DEV VERSION

```
(cd deployment/docker-compose; ./docker-compose-latest up)
```
