#
# COPYRIGHT Ericsson 2023
#
#
#
# The copyright to the computer program(s) herein is the property of
#
# Ericsson Inc. The programs may be used and/or copied only with written
#
# permission from Ericsson Inc. or in accordance with the terms and
#
# conditions stipulated in the agreement/contract under which the
#
# program(s) have been supplied.
#

FROM armdocker.rnd.ericsson.se/proj-eo/common/python:3.7-stretch
RUN groupadd test_admin \
    --gid 1000 && \
    echo 'Group is added'

RUN useradd test_admin \
      --uid 1000 \
      --gid 1000 \
      --create-home && \
      echo 'User is added'

COPY requirements.txt .
RUN pip install -r requirements.txt

USER test_admin
WORKDIR /home/test_admin