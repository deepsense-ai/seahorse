/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

const common = {
  userIP: {
    name: 'User IP',
    instruction: 'IP address of the machine Seahorse is running on. This IP has to be visible from every node of ' +
    'your cluster.',
    placeholder: '',
    type: 'text'
  },
  driverMemory: {
    name: 'Driver Memory',
    instruction: 'Amount of memory to use for the driver process. This is equivalent to the --driver-memory option in ' +
    'spark-submit.',
    placeholder: 'e.g. 1g',
    type: 'text'
  },
  hadoopUser: {
    name: 'Hadoop User',
    instruction: 'Fill this option if you have HDFS on your spark cluster and you want to connect with it in your ' +
    'workflows. This will set HADOOP_USER_NAME environment variable in spark driver.',
    placeholder: 'e.g. hdfs',
    type: 'text'
  },
  executorMemory: {
    name: 'Executor Memory',
    instruction: 'Amount of memory for each executor (e.g. 1000M, 2G). The default is 1G. This is equivalent to ' +
    'the --executor-memory option in spark-submit.',
    placeholder: 'e.g. 2G',
    type: 'text'
  },
  totalExecutorCores: {
    name: 'Total Executor Cores',
    instruction: 'Total number of cores for all executors. This is equivalent to the --total-executor-cores option' +
    ' in spark-submit. ',
    placeholder: 'e.g. 4',
    type: 'number'
  },
  executorCores: {
    name: 'Executor Cores',
    instruction: 'Number of cores per executor. This is equivalent to the --executor-cores option in spark-submit.',
    placeholder: 'e.g. 1',
    type: 'number'
  },
  numExecutors: {
    name: 'Number of Executors',
    instruction: 'Number of executors to launch. The default is 2. This is equivalent to the --num-executors ' +
    'option in spark-submit. ',
    placeholder: 'e.g. 1',
    type: 'number'
  },
  params: {
    name: 'Custom settings',
    instruction: '<div>Here you can specify any additional options to spark-submit. Note that Seahorse runs spark' +
    ' applications in client mode, so not all options will make sense. Also, Seahorse needs to override some ' +
    'settings in order to be fully functional.</div>' +
    '<div>Parameters specified here will override parameters specified in fields above. Each parameter should be ' +
    'defined in separate line.</div>',
    placeholder: 'e.g.\n--conf spark.broadcast.compress=true\n--executor-memory 1G',
    type: 'textarea'
  }
};

const yarn = {
  main: 'YARN',
  uri: {
    name: 'Hadoop Configuration Directory',
    instruction: '<div>In order to run Spark on Yarn you need to provide a directory configuration for your ' +
    'YARN cluster. This directory should contain several xml files (for example: <i>hdfs-site.xml</i>). ' +
    'Please copy the configuration directory to <code>your-seahorse-startup-directory/data/</code> and and point ' +
    'to it in the above field.</div>' +
    '<div> For example, after copying a <code>MyYARN</code> configuration directory to ' +
    '<code>your-seahorse-startup-directory/data/</code>, this field should be filled with ' +
    '<code>/resources/data/MyYARN</code>.</div>',
    placeholder: 'e.g. /resources/data/MyYARN',
    type: 'text'
  }
};

const mesos = {
  main: 'Mesos',
  uri: {
    name: 'Master URL',
    instruction: 'The master URL for the cluster.',
    placeholder: 'e.g. mesos://host:port',
    type: 'text'
  }
};

const standalone = {
  main: 'Stand-alone',
  uri: {
    name: 'Master URL',
    instruction: 'The master URL for the cluster.',
    placeholder: 'e.g. spark://host:port',
    type: 'text'
  }
};

const local = {
  main: 'Local',
  params: {
    name: 'Custom settings',
    instruction: '<div>Here you can specify any additional options to spark-submit. Note that Seahorse runs spark' +
    ' applications in client mode, so not all options will make sense. Also, Seahorse needs to override some ' +
    'settings in order to be fully functional.</div>' +
    '<div>Parameters specified here will override parameters specified in fields above. Each parameter should be ' +
    'defined in separate line.</div>',
    placeholder: 'e.g.\n--conf spark.broadcast.compress=true\n--driver-memory 1G',
    type: 'textarea'
  }
};


exports.inject = function (module) {
  module.constant('PresetModalLabels', {
    yarn: _.assign(yarn, _.pick(common, ['userIP', 'driverMemory', 'hadoopUser', 'executorMemory', 'executorCores',
      'numExecutors', 'params'])),
    mesos: _.assign(mesos, _.pick(common, ['userIP', 'driverMemory', 'hadoopUser', 'executorMemory', 'totalExecutorCores',
      'params'])),
    standalone: _.assign(standalone, _.pick(common, ['userIP', 'driverMemory', 'hadoopUser', 'executorMemory',
      'totalExecutorCores', 'executorCores', 'params'])),
    local: _.assign(local, _.pick(common, ['driverMemory']))
  });
};
