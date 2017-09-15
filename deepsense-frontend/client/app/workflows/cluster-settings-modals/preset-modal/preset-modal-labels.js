'use strict';

exports.inject = function (module) {
  module.constant('PresetModalLabels', {
    yarn: {
      main: 'YARN',
      uri: 'Hadoop Configuration Directory',
      instruction: '<div><b>In order to run Spark on Yarn</b> you need to provide a directory configuration for your ' +
      'YARN cluster - Hadoop Configuration Directory. This directory should contain several' +
      'xml files (for example: hdfs-site.xml).</div>' +
      '<div>Please copy the configuration directory to <code>your-seahorse-startup-directory/data/</code> and point' +
      'to it in the <i>Hadoop Configuration Directory</i> Hadoop Configuration Directory field. For example,' +
      ' after copying a <code>MyYARN</code> configuration directory to <code>your-seahorse-startup-directory/data/</code>, ' +
      '<i>Hadoop Configuration Directory</i> Hadoop Configuration Directory field should be filled with' +
      '<code>your-seahorse-startup-directory/data/MyYARN</code>.</div>',
      executorMemory: 'Executor memory',
      totalExecutorCores: 'Total executor cores',
      numExecutors: 'Number of executors'
    },
    mesos: {
      main: 'Mesos',
      uri: 'Spark Master Uri',
      executorMemory: 'Executor memory',
      totalExecutorCores: 'Total executor cores'
    },
    standalone: {
      main: 'Stand-alone',
      uri: 'Spark Master Uri',
      executorMemory: 'Executor memory',
      totalExecutorCores: 'Total executor cores',
      executorCores: 'Executor cores'
    }
  });
};
