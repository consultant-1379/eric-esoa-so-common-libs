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

import jsonschema
import json


def validate_logs(log_file, file_to_validate):
    schema = read_schema(file_to_validate)
    return check_log_file(log_file, schema)


def read_schema(file_to_validate):
    # TODO(eankinn) change hardcode of the path to configurable settings of the test
    with open(file_to_validate, 'r') as f:
        schema_data = f.read()
    schema = json.loads(schema_data)
    return schema


def check_log_file(file_name, schema):
    try:
        with open(file_name, 'r') as f:
            line = f.readline()
            while line:
                log = json.loads(line)
                jsonschema.validate(log, schema)
                line = f.readline()
        return True
    except jsonschema.exceptions.ValidationError as e:
        print("well-formed but invalid JSON:", e)
        return False
    except json.decoder.JSONDecodeError as e:
        print("poorly-formed text, not JSON:", e)
        return False
