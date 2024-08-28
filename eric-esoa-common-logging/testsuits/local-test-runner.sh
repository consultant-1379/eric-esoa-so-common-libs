#!/usr/bin/env bash
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

testedServicesFolder='services'
loggingLibraryPomFileName='logging-library-pom.xml'
library_jar_folder='../target'
library_pom_folder='..'
maven_settings_path="${M2_HOME}"
#########################
# The command line help #
#########################
display_help() {
  echo "Usage: ${0} [options...] "
  echo
  echo "   --start,              run docker-compose"
  echo "   --stop,               stop docker-compose"
  echo "   --prepare,            prepare workspace by coping eo-logging-library jar file from target folder and pom.xml from base directory"
  echo "   --clean,              clean workspace after tests, remove all additional jar and pom files, that were added. Also revert docker-compose file to original state"
  echo "   --logs                view logs from tests. Use option --all to view logs from all containers"
  echo "   --help, -h            display help information"
  echo
}

display_help_for_prepare() {
  echo "Usage: ${0} --prepare [options...] "
  echo
  echo "   --jar-dir=[PATH_TO_FILE]    path to the folder with library jar file"
  echo "   --pom-dir=[PATH_TO_FILE]    path to the folder with library pom file"
  echo "   --maven-settings-path=[PATH_TO_FILE] path to the maven settings file"
  echo "   --help, -h                           display help information"
  echo
}

show_logs() {
  local show_all="$1"
  echo '==============Tests logs============================='
  docker-compose logs --timestamps --no-color | sed 's/^integration-tests_1[ ]*|//'
  if [ -n "${show_all}" ]; then
    echo '==============Service logs============================'
    ls logs/*.log | while read file; do
      echo "=====Log from service ${file}========"
      cat "${file}"
    done
  fi
}

prepare_workspace() {
  echo 'Get version of eo-logging-library that was built at the previous step'
  LOGGING_LIBRARY_VERSION="$(ls ${library_jar_folder}/*.jar | sed -E 's|.*[a-z][0-9]?-(.*).jar$|\1|')"
  sed -i 's|lib_version|'"$LOGGING_LIBRARY_VERSION"'|g' docker-compose.yml
  if [ ${?} -gt 0 ]; then
    echo "Can not detect version of logging library. Sed result is: ${?}"
    exit ${?}
  fi
  echo "LOGGING_LIBRARY_VERSION: ${LOGGING_LIBRARY_VERSION}"
  sed -i -E 's|(LOGGING_LIBRARY_VERSION: )"LATEST"|\1'"$LOGGING_LIBRARY_VERSION"'|' docker-compose.yml
  if [ ${?} -gt 0 ]; then
    echo "Can not setup version of logging library in docker-compose. Sed result is: ${?}"
    exit ${?}
  fi

  ls "${testedServicesFolder}"/*/ -d | while read service; do
    prepare_workspace_item "${service}"
  done
}

 clean_workspace() {
  LOGGING_LIBRARY_VERSION="$(ls "${library_jar_folder}"/*.jar | sed -E 's|.*[a-z][0-9]?-(.*).jar$|\1|')"
  sed -i -E 's|(LOGGING_LIBRARY_VERSION: )'"${LOGGING_LIBRARY_VERSION}"'|\1"LATEST"|' docker-compose.yml
  if [ ${?} -gt 0 ]; then
    echo "WARNING!!! Can not rollback version of logging library in docker-compose. Sed result is: ${?}. File is changed, please make sure that you will not push it to the remote repo"
  fi
  ls "${testedServicesFolder}" | while read service; do
    clean_workspace_item "${testedServicesFolder}/${service}"
  done

}

prepare_workspace_item() {
  local serviceWithLogsFolder="$1"
  echo "========= preparing namespace for service from ${serviceWithLogsFolder} ============="
  echo 'Add maven setting to container for building test service with logging-library'
  if ! cp "${maven_settings_path}/settings.xml" "${serviceWithLogsFolder}"; then
    echo "Can not copy maven settings file from ${maven_settings_path}. Use the option --maven-settings-path to provide correct path to maven setting.xml"
    exit 1
  fi
  echo 'Add just build version of eo-logging-library to local .m2 of testing service'
  if ! cp "${library_jar_folder}"/*.jar "${serviceWithLogsFolder}"; then
    echo "Can not copy jar with logging library from directory ${library_jar_folder}"
    exit 1
  fi
  echo 'Add pom.xml of eo-logging-library to local .m2 of testing service'
  if ! cp "${library_pom_folder}"/pom.xml "${serviceWithLogsFolder}"/${loggingLibraryPomFileName}; then
    echo "Can not copy pom.xml of logging library from directory ${library_pom_folder}"
    exit 1
  fi
}

clean_workspace_item() {
  local serviceWithLogsFolder="$1"
  echo "========= cleaning namespace for service from ${serviceWithLogsFolder} ============="
  echo 'Remove pom.xml of logging-library from test folder'
  rm -rf "${serviceWithLogsFolder}/${loggingLibraryPomFileName}"
  echo 'Remove settings.xml of logging-library from test folder'
  rm -rf "${serviceWithLogsFolder}/settings.xml"
  echo 'Remove jars of logging-library from test folder'
  rm -rf "${serviceWithLogsFolder}"/*.jar
}

start() {
  sed -i -e "s|\(--uid\) 1000|\1\ $(id -u)|" -e "s|\(--gid\) 1000|\1\ $(id -g)|" ./integration-tests/Dockerfile
  sed -i -e "s|\(UID: \)1000 # <-- gets substituted.*|\1\ $(id -u)|" -e "s|\(GID: \)1000 # <-- gets substituted.*|\1\ $(id -g)|" docker-compose.yml
  #  Can't use docker-compose build --parallel in Jenkins, because of old version of docker-compose
  if ! docker-compose up --build -d; then
    docker-compose logs --timestamps --no-color
    exit 1
  fi
}

stop() {
  if ! docker-compose down; then
    echo 'There is a problem with stopping tests'
  fi
}

show_logs() {
  local show_all="$1"
  echo '==============Tests logs============================='
  docker-compose logs --timestamps --no-color | sed 's/^integration-tests_1[ ]*|//'
  if [ -n "${show_all}" ]; then
    echo '==============Service logs============================'
    ls logs/*.log | while read file; do
      echo "=====Log from service ${file}========"
      cat "${file}"
    done
  fi
}

while [ ${#} -ne 0 ]; do
  case "${1}" in
  --start)
    start
    ;;
  --stop)
    stop
    ;;
  --logs)
    if [ -n "${2:-}" ] && [ "${2}" = '--all' ]; then
      show_logs True
      shift
    else
      show_logs
      shift
    fi
    ;;
  --prepare)
    for arg in "$@"; do
      set -- $(echo "$arg" | tr '=' ' ')
      key=$1
      if [ "${key}" == '--help' ] || [ "${key}" == '-h' ]; then
        display_help_for_prepare
        exit 0
      else
        value=$2
        if [ "${key}" == "--jar-dir" ]; then
          library_jar_folder="${value}"
        elif [ "${key}" == "--pom-dir" ]; then
          library_pom_folder="${value}"
        elif [ "${key}" == "--maven-settings-path" ]; then
          maven_settings_path="${value}"
        fi
      fi
    done
    prepare_workspace
    ;;
  --clean)
    clean_workspace
    ;;
  --help | -h)
    display_help
    exit 0
    ;;
  esac
  shift
done

exit 0
