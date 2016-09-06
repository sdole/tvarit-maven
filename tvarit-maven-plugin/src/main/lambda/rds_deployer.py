from __future__ import print_function

import boto3

import util


def handler(event, context):
    app_metadata = util.get_app_metadata(event)
    rds_version = app_metadata['Metadata']['db_version']
    rds_client = boto3.client("rds")
    all_db_instances = rds_client.describe_db_instances()
    for each_db_instance in all_db_instances:
        all_tags = each_db_instance['Tags']
        if "version" in all_tags and all_tags['version'] == rds_version:
            # version found... no need to create new - just send sns notification to kick off app deploy
            print("found!")
        else:
            # first find template, create new rds and send notification via cloudformation
            print("not found!")


if __name__ == "main":
    handler({}, {})
