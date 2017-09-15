Dependencies

   You need to place following files in src/universal directory

   1.1 Session Executor jar (we.jar)
       NOTE: Python path in application.conf needs to be set to /opt/conda/bin/python
         It might require manual change prop pythoncaretaker.python-binary in application.conf in JAR.
   1.2 Session Executor dependencies zip (we-deps.zip)
       NOTE: Python paths in start.sh, kernel.json etc need to be set to /opt/conda

Publish docker

```
sbt sessionmanager/docker:publishLocal
```
