# Copyright (c) 2015, CodiLime Inc.

FROM ubuntu:14.04

RUN apt-get update && apt-get install keystone -y

COPY run.sh /tmp/
COPY keystone.db /tmp/keystone.db.empty

EXPOSE 35357
EXPOSE 5000

ENTRYPOINT ["/bin/bash", "/tmp/run.sh"]

CMD ["/bin/bash"]
