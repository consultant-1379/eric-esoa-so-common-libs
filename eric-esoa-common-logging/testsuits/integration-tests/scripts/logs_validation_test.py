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

import pytest
import time
from validate_logs import validate_logs


def test_service(url,check_logs_from, file_to_validate):
    time.sleep(10)
    assert validate_logs(check_logs_from, file_to_validate) is True
