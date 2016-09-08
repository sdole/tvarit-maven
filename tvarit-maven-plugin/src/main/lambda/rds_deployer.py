from __future__ import print_function

import os;

import boto3

import util

cfn_client = boto3.client("cloudformation")


def deploy(event, context):
    app_metadata = util.get_app_metadata(event)
    rds_version = app_metadata['Metadata']['db-version']
    rds_client = boto3.client("rds")
    all_db_instances = rds_client.describe_db_instances()
    found_db = False
    if len(all_db_instances['DBInstances']) > 0:
        for each_db_instance in all_db_instances['DBInstances']:
            all_tags = each_db_instance['Tags']
            if "version" in all_tags and all_tags['version'] == rds_version:
                # version found... no need to create new - just send sns notification to kick off app deploy
                print("found!")
                found_db = True
            else:
                # first find template, create new rds and send notification via cloudformation
                print("not found!")
                found_db = False

    if not found_db:
        rds_template = util.make_cfn_url("app/rds.template")
        group_id = app_metadata['Metadata']["group-id"]
        artifact_id = app_metadata['Metadata']["artifact-id"]
        version = app_metadata['Metadata']["version"]
        network_resources = util.make_resources_map_from_cfn("Network")
        db_subnet_group = network_resources["DbSubnetGroupOutput"]

        rds_stack_parameters = [
            {"ParameterKey": "DbSubnetGroupNameParm", "ParameterValue": db_subnet_group},
        ]
        sns_resources = util.make_resources_map_from_cfn("SnsTopics")
        deploy_complete_topic = sns_resources["ProvisioningAutomationSnsTopic"]

        stack_name = ("rds" + group_id + "-" + artifact_id + "-" + version).replace(".", "-")
        cfn_client.create_stack(
            StackName=stack_name,
            TemplateURL=rds_template,
            Parameters=rds_stack_parameters,
            NotificationARNs=[deploy_complete_topic]
        )


if __name__ == "__main__":
    os.environ['AWS_DEFAULT_REGION'] = 'us-east-1'
    deploy({
        "Records": [
            {
                "eventVersion": "2.0",
                "eventTime": "2016-08-15T12:40:54.623Z",
                "requestParameters": {
                    "sourceIPAddress": "70.194.73.255"
                },
                "s3": {
                    "configurationId": "4334e041-7e25-4fcb-80dd-2852b9d84e05",
                    "object": {
                        "eTag": "b39a33b8bfa97f473337ff5af051c1b1",
                        "sequencer": "0057B1B8567534B9AC",
                        "key": "deployables/Capture.war",
                        "size": 72804
                    },
                    "bucket": {
                        "arn": "arn:aws:s3:::tvarit-tvarit-tomcat-plugin-test",
                        "name": "tvarit-tvarit-tomcat-plugin-test",
                        "ownerIdentity": {
                            "principalId": "A1QW81VB1IU6AT"
                        }
                    },
                    "s3SchemaVersion": "1.0"
                },
                "responseElements": {
                    "x-amz-id-2": "LVxXRpJkMOwK0znpxC5Bddu1/IsNGDVU6iJB1qCZqf8L1Sv7J0tktyHLpypJdZ8K",
                    "x-amz-request-id": "C80A5A089D74B47E"
                },
                "awsRegion": "us-east-1",
                "eventName": "ObjectCreated:Put",
                "userIdentity": {
                    "principalId": "A1QW81VB1IU6AT"
                },
                "eventSource": "aws:s3"
            }
        ]
    }, {})
