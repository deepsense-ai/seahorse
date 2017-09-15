# GETTING STARTED WITH DEVELOPMENT

```
# Init nested workflow-executor repo
git submodule init
git submodule update

# Compile classes from workflow-executor that are needed to compile deepsense-backend
sbt publishWeClasses
```

# START SEAHORSE LATEST DEV VERSION

```
(cd deployment/docker-compose; ./docker-compose-latest up)
```