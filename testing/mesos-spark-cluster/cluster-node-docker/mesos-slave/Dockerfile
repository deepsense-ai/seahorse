# Copyright (c) 2016, CodiLime Inc.
# NOTE: based on https://github.com/mesoscloud/mesos-master and https://github.com/mesoscloud/mesos-slave

FROM seahorse/docker-mesos-master:local
MAINTAINER DeepSense.io

ENV MESOS_CONTAINERIZERS mesos

# https://mesosphere.github.io/marathon/docs/native-docker.html
ENV MESOS_EXECUTOR_REGISTRATION_TIMEOUT 5mins

# https://issues.apache.org/jira/browse/MESOS-4675
ENV MESOS_SYSTEMD_ENABLE_SUPPORT false

COPY entrypoint-slave.sh /

ENTRYPOINT ["/usr/local/bin/dumb-init", "/entrypoint-slave.sh"]
# We need to override default Mesos slave PATH environment variable to make R visible for Seahorse Session Executor
CMD ["/usr/sbin/mesos-slave", "--executor_environment_variables={\"PATH\": \"/opt/conda/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin\"}"]
