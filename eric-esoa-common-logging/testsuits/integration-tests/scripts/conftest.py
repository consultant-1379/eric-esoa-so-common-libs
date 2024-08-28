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

def pytest_addoption(parser):
    parser.addoption(
        "--url",
        action="append",
        default=[],
        help="list of urls to pass to test functions",
    )
    parser.addoption(
        "--check_logs_from",
        action="append",
        default=[],
        help="list of log_file to pass to test functions",
    )
    parser.addoption(
        "--file_to_validate",
        action="append",
        default=[],
        help="list of file to pass to log validation",
    )


def create_parameters(urls, logs, file_to_validate):
    if len(urls) != len(logs):
        raise Exception('number of service urls and log files to check has to be equal')
    return zip(urls, logs, file_to_validate)


def pytest_generate_tests(metafunc):
    if "url" in metafunc.fixturenames and "check_logs_from" in metafunc.fixturenames:
        urls = metafunc.config.getoption("url")
        logs = metafunc.config.getoption("check_logs_from")
        file_to_validate = metafunc.config.getoption("file_to_validate")
        args = create_parameters(urls, logs, file_to_validate)
        metafunc.parametrize('url,check_logs_from,file_to_validate', args)
