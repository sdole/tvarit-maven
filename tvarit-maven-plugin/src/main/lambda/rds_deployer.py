from __future__ import print_function

import json
import os

import boto3

import util

cfn_client = boto3.client("cloudformation")
rds_client = boto3.client("rds")


def deploy(event, context):
    print(json.dumps(event, indent=4, sort_keys=True, default=lambda x: str(x)))
    deployable_bucket_name = event["Records"][0]["s3"]["bucket"]["name"]
    deployable_key_name = event["Records"][0]["s3"]["object"]["key"]
    app_metadata = util.get_app_metadata(
        deployable_bucket_name,
        deployable_key_name
    )
    print(json.dumps(app_metadata, indent=4, sort_keys=True, default=lambda x: str(x)))
    rds_version = app_metadata['Metadata']['db-version']
    print("rds version is: " + str(rds_version))
    all_db_instances = rds_client.describe_db_instances()
    print(json.dumps(all_db_instances, indent=4, sort_keys=True, default=lambda x: str(x)))
    found_db = False
    if len(all_db_instances['DBInstances']) > 0:
        print("inside for - found at least one db instance")
        for each_db_instance in all_db_instances['DBInstances']:
            print("inside if - iterating over dbs")
            all_tags = each_db_instance['Tags'] if "Tags" in each_db_instance else {}
            print(json.dumps(all_tags, indent=4, sort_keys=True, default=lambda x: str(x)))
            if "tvarit_version" in all_tags and all_tags['tvarit_version'] == rds_version:
                print("found the version tag and it was same as rds version " + rds_version)
                # version found... no need to create new - just send sns notification to kick off app deploy
                found_db = True
            else:
                # first find template, create new rds and send notification via cloudformation
                print("db not found!")
                found_db = False

    if not found_db:
        rds_template = util.make_cfn_url("app/rds.template")
        group_id = app_metadata['Metadata']["group-id"]
        artifact_id = app_metadata['Metadata']["artifact-id"]
        version = app_metadata['Metadata']["version"]
        print("db to be created: group, artifact, version: " + group_id + " : " + artifact_id + " : " + version)

        network_resources = util.make_resources_map_from_cfn("Network")
        print(json.dumps(network_resources, indent=4, sort_keys=True, default=lambda x: str(x)))
        db_subnet_group = network_resources["DbSubnetGroupOutput"]

        rds_stack_parameters = [
            {"ParameterKey": "DbSubnetGroupNameParm", "ParameterValue": db_subnet_group},
            {"ParameterKey": "DbVersionParm", "ParameterValue": rds_version}
        ]
        print("printing rds_stack_parameters")
        print(json.dumps(rds_stack_parameters, indent=4, sort_keys=True, default=lambda x: str(x)))
        sns_resources = util.make_resources_map_from_cfn("SnsTopics")
        deploy_complete_topic = sns_resources["ProvisioningAutomationSnsTopic"]

        stack_name = ("rds" + group_id + "-" + artifact_id + "-" + version).replace(".", "-")
        cfn_client.create_stack(
            StackName=stack_name,
            TemplateURL=rds_template,
            Parameters=rds_stack_parameters,
            NotificationARNs=[deploy_complete_topic],
            Tags=[{"Key": "app_file_object", "Value": deployable_bucket_name + "::" + deployable_key_name}]
        )
    else:
        print("found db of same version, so nothing to do.")


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
