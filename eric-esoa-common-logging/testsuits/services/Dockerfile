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

#Decided to build test service in test to avoid creating separate repo, pushing to nexus, create pipeline etc
#To build library service has to have artifact from eo repo, that is why custom setting.xml was added
ARG JAVA_VERSION="17"

FROM armdocker.rnd.ericsson.se/proj-ldc/common_base_os/sles:6.1.0-2 as build
ARG JAVA_VERSION
RUN if [ "${JAVA_VERSION}" = "1_8_0" ] ; then \
      zypper --quiet ar -C -G -f https://arm.rnd.ki.sw.ericsson.se/artifactory/proj-ldc-repo-rpm-local/common_base_os/sles/5.23.0-0_devel/?ssl_verify=no LDC-SLES && \
      zypper --quiet ref -f --repo LDC-SLES && \
      zypper --quiet install --auto-agree-with-licenses --no-confirm -f shadow java-"${JAVA_VERSION}"-openjdk-devel ca-certificates-mozilla && \
      zypper --quiet clean --all && \
      rm -rf /var/cache/zypp; \
    else \
      zypper --quiet ar -C -G -f https://arm.rnd.ki.sw.ericsson.se/artifactory/proj-ldc-repo-rpm-local/common_base_os/sles/5.23.0-0/?ssl_verify=no LDC-SLES && \
      zypper --quiet ar -C -G -f https://arm.rnd.ki.sw.ericsson.se/artifactory/proj-ldc-repo-rpm-local/common_base_os/sles/5.23.0-0_devel/?ssl_verify=no LDC-SLES_DEVEL && \
      zypper --quiet ref -f --repo LDC-SLES && \
      zypper --quiet ref -f --repo LDC-SLES_DEVEL && \
      zypper --quiet install --auto-agree-with-licenses --no-confirm -f shadow java-"${JAVA_VERSION}"-openjdk-devel ca-certificates-mozilla && \
      zypper --quiet clean --all && \
      rm -rf /var/cache/zypp; \
    fi

RUN groupadd logging_service_admin --gid 1000 && \
      useradd logging_service_admin --uid 1000 --gid 1000 --create-home

USER logging_service_admin
WORKDIR /home/logging_service_admin

ARG SOURCE_FOLDER
COPY --chown=logging_service_admin:logging_service_admin "${SOURCE_FOLDER}" .

ARG LOGGING_LIBRARY_VERSION
RUN ./mvnw -q -s settings.xml install:install-file \
               -Dfile="${PWD}"/eric-common-logging-"${LOGGING_LIBRARY_VERSION}".jar \
               -DpomFile="${PWD}"/logging-library-pom.xml \
            package -DskipTests -Deric-common-logging.version="${LOGGING_LIBRARY_VERSION}" && \
            mkdir -p target/dependency && \
            (cd target/dependency; jar -xf ../*.jar)


FROM armdocker.rnd.ericsson.se/proj-ldc/common_base_os/sles:6.1.0-2
ARG JAVA_VERSION
RUN zypper --quiet ar -C -G -f https://arm.rnd.ki.sw.ericsson.se/artifactory/proj-ldc-repo-rpm-local/common_base_os/sles/5.23.0-0?ssl_verify=no LDC-SLES && \
    zypper --quiet ref -f --repo LDC-SLES && \
    zypper --quiet install --auto-agree-with-licenses --no-confirm -f shadow file-magic java-"${JAVA_VERSION}"-openjdk-headless && \
    zypper --quiet clean --all && \
    rm -rf /var/cache/zypp

ARG UID=1000
ARG GID=1000
RUN groupadd logging_service_admin --gid $GID && \
      useradd logging_service_admin --uid $UID --gid $GID --create-home
USER logging_service_admin
WORKDIR /home/logging_service_admin

COPY --from=build --chown=logging_service_admin:logging_service_admin /home/logging_service_admin/commands.sh .
ENTRYPOINT ["./commands.sh"]

ARG DEPENDENCY=/home/logging_service_admin/target/dependency
COPY --from=build --chown=logging_service_admin:logging_service_admin ${DEPENDENCY}/BOOT-INF/lib app/lib
COPY --from=build --chown=logging_service_admin:logging_service_admin ${DEPENDENCY}/META-INF app/META-INF
COPY --from=build --chown=logging_service_admin:logging_service_admin ${DEPENDENCY}/BOOT-INF/classes app
