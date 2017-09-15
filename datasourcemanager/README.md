## DEV MANUAL

# Run datasourcemanager in dev:

```
sbt datasourcemanager/runtime:run
wget http://localhost:8080/datasources
```

# Build docker:
```
sbt datasourcemanager/docker:publishLocal
```