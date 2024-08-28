# Acceptance test for ESOA Common Logging Library

## Idea
These tests check that after adding esoa-logging-library as a dependency to the service:
* it can be started
* it has logs in JSON format
* JSON format follows ADP schema and has additional fields, that Vortex team added.
See more details about ADP Schema: [ADP Schema](https://gerrit.ericsson.se/plugins/gitiles/bssf/adp-log/api/+/v1.2.0/schema/log-event/src/main/resources/log-event.1.json)
See more details about extra fields that team Vortex added: [Vortex Schema](integration-tests/scripts/adp-log-schema-1_2_0.json)

Fields that were added by Vortex team:
* correlation_id - UUID to track a request that was sent between different services
* user - name of a user, that sent request
* path - a path of an endpoint, that was called in a service

**correlation_id** and **path** should be in logs, if it was produced by classes, that process http requests.
**user** should be in logs, if information about user exists.

Tests check:
* that service is available by sending http HEAD request. Tests use retry mechanism, if service is unavailable
* analyze a file with service logs, by comparing each row with JSON-schema.

Tests have 2 input parameters:
* url - URL of the service, to check that it is available and produce logs of processing http request in the service
* path to the file with service logs

Environment to run tests and services is described in [docker-compose.yml](docker-compose.yml)

Tests are written on Python 3.2 and use [pytest framework](https://docs.pytest.org/en/latest/)

## Version of the library, that will be tested

Services have to use the version of the library, that exists in the current commit.
Because of this:
* in pom.xml of the service library version is not specified
* jar file with the library will be taken from the target directory. It is assumed, that this is just a built version of the library with the latest changes.
* pom.xml of the library will be taken from {project.base_dir}. It is needed to be able later to add the library to local maven repo of the service

## Run locally

To run test locally [local-test-runner.sh](local-test-runner.sh) script can be used.
To see how to use it just run
```
$ local-test-runner.sh --help
```
General procedure to run test

Pre-conditions:
1. Build esoa-logging-library or have a folder with esoa-logging-library-{version}.jar and pom.xml for this version

Steps:

**Step 1**: Run ./local-test-runner.sh --prepare --maven-settings-path={path_to_folder_with_your_settings.xml}

If esoa-logging-library-{version}.jar and pom.xml are in a particular folder(s), use:
   * --jar-dir={path_to_the_folder_with_jar} - if there is esoa-logging-library-{version}.jar in separate folder, that should be used in test.
   Default value: {project.base_dir}/target
   * --pom-dir={path_to_the_folder_with_jar} - if there is pom.xml of esoa-logging-library in separate folder, that should be used in test
   Default value: {project.base_dir}

   *IMPORTANT*: pom.xml should be from the same version as esoa-logging-library.jar file

This step will copy required files (setting.xml, esoa-logging-library.jar, pom.xml) to each test service and change docker-compose.yml to set up **LOGGING_LIBRARY_VERSION** argument. 
When service is built, esoa-logging-library.jar, pom.xml will be used by maven install plugin to add files in maven local repo to build service.
Setting.xml will be used by maven to build service as well.

**Step 2**: After the step 1 environment should be ready to start services and tests.
Run ./local-test-runner.sh --start to start services and tests

*IMPORTANT*: script run just docker-compose up --build -d command. If it needed to run docker-compose in another way, it can be done and there is no need to run this stage of the script.

**Step 3**: Run ./local-test-runner.sh --stop to stop all services and tests

This step just stop containers. After this step "Step 2" can be run again.

**Step 4**: Run ./local-test-runner.sh --clean when testing is completely finish for one the version of esoa-logging-library
Environment will be cleaned:
1. esoa-logging-library-{version}.jar, will be deleted from each service folder
2. logging-library-pom.xml, will be deleted from each service folder
3. setting.xml, will be deleted from each service folder
4. in docker-compose.yml **LOGGING_LIBRARY_VERSION** will be rollback to "LATEST" value

**Additional Step**: Run ./local-test-runner.sh --logs to see test results

##Add new service to test
1. add new folder inside /service folder
2. add new service to docker-compose.yml
3. add new service into *depends_on* section of integration-tests in docker-compose.yml
4. add new parameters in *command* section of integration-tests in docker-compose.yml
```
      - '--url=http://{new_service_name}:{new_service_port}/{path_to_head_endpoint}/'
      - '--check_logs_from={path_to_file_with_logs}'
```