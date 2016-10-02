from __future__ import print_function

import json
import os

import boto3

import util

cfn_client = boto3.client("cloudformation")
rds_client = boto3.client("rds")
sqs_client = boto3.client("sqs")


def deploy(event, context):
    print(json.dumps(event, indent=4, sort_keys=True, default=lambda x: str(x)))
    deployable_bucket_name = event["Records"][0]["s3"]["bucket"]["name"]
    deployable_key_name = event["Records"][0]["s3"]["object"]["key"]
    app_metadata = util.get_app_metadata(
        deployable_bucket_name,
        deployable_key_name
    )
    rds_version = app_metadata['Metadata']['db-version']
    db_name = app_metadata['Metadata']['db-name']
    db_username = app_metadata['Metadata']['db-username']
    db_password = app_metadata['Metadata']['db-password']
    all_db_instances = rds_client.describe_db_instances()
    found_db = False
    if len(all_db_instances['DBInstances']) > 0:
        for each_db_instance in all_db_instances['DBInstances']:
            all_tags = each_db_instance['Tags'] if "Tags" in each_db_instance else {}
            if "tvarit_version" in all_tags and all_tags['tvarit_version'] == rds_version:
                found_db = each_db_instance["Endpoint"]
            else:
                found_db = False

    sns_resources = util.make_base_output_map_from_cfn("SnsTopics")
    deploy_complete_topic = sns_resources["RdsDeployedSnsTopicOutput"]
    rds_already_deployed_sqs_q = sns_resources["RdsAlreadyDeployedSqsTopicOutput"]
    if not found_db:
        print("db not found!")
        rds_template = util.make_cfn_url("app/rds.template")
        group_id = app_metadata['Metadata']["group-id"]
        artifact_id = app_metadata['Metadata']["artifact-id"]
        version = app_metadata['Metadata']["version"]
        print("db to be created: group, artifact, version: " + group_id + " : " + artifact_id + " : " + version)

        network_resources = util.make_base_output_map_from_cfn("Network")
        print(json.dumps(network_resources, indent=4, sort_keys=True, default=lambda x: str(x)))
        db_subnet_group = network_resources["DbSubnetGroupOutput"]
        db_security_group = network_resources["DbSecurityGroupOutput"]

        rds_stack_parameters = [
            {"ParameterKey": "DbSubnetGroupNameParam", "ParameterValue": db_subnet_group},
            {"ParameterKey": "DbVersionParam", "ParameterValue": rds_version},
            {"ParameterKey": "DbNameParam", "ParameterValue": db_name},
            {"ParameterKey": "DbUsernameParam", "ParameterValue": db_username},
            {"ParameterKey": "DbPasswordParam", "ParameterValue": db_password},
            {"ParameterKey": "DbSecurityGroupParam", "ParameterValue": db_security_group}
        ]
        print("printing rds_stack_parameters")
        print(json.dumps(rds_stack_parameters, indent=4, sort_keys=True, default=lambda x: str(x)))

        stack_name = ("rds" + group_id + "-" + artifact_id + "-" + version).replace(".", "-")
        cfn_client.create_stack(
            StackName=stack_name,
            TemplateURL=rds_template,
            Parameters=rds_stack_parameters,
            NotificationARNs=[deploy_complete_topic],
            Tags=[{"Key": "app_file_object", "Value": deployable_bucket_name + "::" + deployable_key_name}]
        )
    else:
        print("found the version tag and it was same as rds version " + rds_version)
        sqs_client.send_message(QueueUrl=rds_already_deployed_sqs_q, MessageBody=json.dumps(found_db))


if __name__ == "__main__":
    os.environ['AWS_DEFAULT_REGION'] = 'us-east-1'
    deploy({
        "Records": [
            {
                "awsRegion": "us-east-1",
                "eventName": "ObjectCreated:Put",
                "eventSource": "aws:s3",
                "eventTime": "2016-09-11T03:06:30.601Z",
                "eventVersion": "2.0",
                "requestParameters": {
                    "sourceIPAddress": "67.184.32.58"
                },
                "responseElements": {
                    "x-amz-id-2": "ILhaJLjTHhDD/BmBS8lN3PGtLpXo11O26etki1q2H1lcJ4iiJDFDQcD4k4e0FhERq7BuKNkMGXY=",
                    "x-amz-request-id": "1C17AA6EE4AD3630"
                },
                "s3": {
                    "bucket": {
                        "arn": "arn:aws:s3:::tvarit-tvarit-tomcat-plugin-test",
                        "name": "tvarit-tvarit-tomcat-plugin-test",
                        "ownerIdentity": {
                            "principalId": "A1QW81VB1IU6AT"
                        }
                    },
                    "configurationId": "1f5b8622-2a0d-4bce-a742-5ef8b4283573",
                    "object": {
                        "eTag": "0805de1b034a94f867a1d96347deb844",
                        "key": "deployables/tvarit/tomcat-plugin-test/1.0.1-SNAPSHOT/tomcat-plugin-test-1.0.1-SNAPSHOT.war",
                        "sequencer": "0057D4CA3622203077",
                        "size": 101167
                    },
                    "s3SchemaVersion": "1.0"
                },
                "userIdentity": {
                    "principalId": "AWS:AIDAJVZBSURWE64MNEL2M"
                }
            }
        ]
    }
        , {})
