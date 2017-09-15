<!--
  Copyright (c) 2015, CodiLime Inc.
-->

# Framework for DeepSense system tests

This directory contains system tests for Workflow Executor and DS Studio.

System tests are automatically published to Artifactory after merging changes to master
and version branches.

To manually publish system test framework to artifactory, run:
```
./publish.sh ds_studio|workflow_executor ARTIFACT_VERSION [--draft]
```

If flag --draft is specified, system tests will be published to deepsense-system-tests-dev
repository instead of deepsense-system-tests-release. This mode is suited for system test
development. After publishing in draft versions, you can either use the Jenkins job
"Workflow Executor (Development of system tests)" with your uploaded Workflow Executor
or run the system tests on your local machine.

To run the system tests locally, do the following:

To install, run:
```
pip install -r requirements.txt
```
To execute tests, move to dir that contains _tests_ dir, and run
```
pybot tests/ExperimentExecution.robot
```
To run only selected test case, move to dir that contains _tests_ dir, and run
```
pybot -t  DemandForecasting tests/RealLifeCases.robot
```

You can configure system that you want to test using ``settings.conf``.
You can provide your own configuration file by writing its path to environment variable 'ST_CONFIG'.

## Workflow Executor tests

To execute Workflow Executor system tests, you need to provide path to Workflow Executor jar
in your settings file.

You might have to change HDFS host name and local file paths in workflow files to successfully
execute the system tests on your local machine.

Also, the _spark_submit_ command must be globally visible.

System tests check execution reports by matching them to a report pattern file. Report pattern files
consist of a dictionary mapping node UUIDs to lists of entity patterns. Every dictionary declared
as an entity pattern must be contained in the returned report's `resultEntities` section.

Example:

```
{
  "a7f8fbd0-f4c9-429c-9c3f-f32c06ef4ea0": [
    {
      "report": {
        "distributions": {
          "rating": {},
          "review_count": {},
          "smokingUnknown": {},
          "petsUnknown": {},
          "stars": {},
          "deposit": {},
          "petsTRUE": {},
          "smokingTRUE": {},
          "lowest_price": {},
          "highest_price": {}
        },
        "name": "DataFrame Report",
        "tables": {
          "DataFrame Size": {
            "name": "DataFrame Size",
            "columnNames": [
              "Number of columns",
              "Number of rows"
            ],
            "values": [
              [
                "29",
                "3"
              ]
            ]
          },
          "Data Sample": {
            "name": "Data Sample"
          }
        }
      }
    }
  ]
}
```

## DS Studio tests

To execute DS Studio system tests on development environment, you need to run Experiment Manager,
Entity Storage and Deploy Model Service locally.

```
export ST_CONFIG=/path/to/my/settings_file.conf
```
In settings, in section _keystone_, you can set option _insecure_ to _true_ or _false_.
If you set it to _true_, you have to provide field _insecureToken_. Otherwise, you have to
provide _user_, _tenant_ and _password_.

## Useful links

* [robotframework documentation](http://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html)
* [robotframework quickstart guide](https://github.com/robotframework/QuickStartGuide/blob/master/QuickStart.rst)
