from __future__ import print_function

import json
import os

import boto3
import datetime

import plugin_config

json.JSONEncoder.default = lambda self, obj: (obj.isoformat() if isinstance(obj, datetime.datetime) else None)

cfn_client = boto3.client('cloudformation')
s3 = boto3.client('s3')
asg_client = boto3.client('autoscaling')
ec2 = boto3.client('ec2')


def find_template():
    print("in find template")


def modify_template():
    print("in modify template")


def execute_stack():
    print("in execute stack")


def do_instances_exist(app_instance_tag):
    instances = ec2.describe_instances(Filters=[{'Name': 'tag-key', 'Values': [app_instance_tag]}])
    return len(instances['Reservations'][0]['Instances']) > 0


def do_router_exists():
    tags = asg_client.describe_tags(Filters=[
        {
            "Name": "key",
            "Values": ["tvarit:purpose"]
        },
        {
            "Name": "value",
            "Values": ["router"]
        }
    ])

    instances = ec2.describe_instances(Filters=[{'Name': 'tag:key', 'Values': ["tvarit:purpose=router"]}])
    print(json.dumps(instances))

    return len(instances['Reservations']) > 0 and len(instances['Reservations'][0]['Instances']) > 0


def ensure_router_auto_scaling_group_has_instances():
    tags = asg_client.describe_tags(Filters=[
        {
            "Name": "key",
            "Values": ["tvarit:purpose"]
        },
        {
            "Name": "value",
            "Values": ["router"]
        }
    ])

    router_asg_name = tags['Tags'][0]['ResourceId']

    auto_scaling_groups = asg_client.describe_auto_scaling_groups(AutoScalingGroupNames=[router_asg_name])
    if auto_scaling_groups['AutoScalingGroups'][0]['MaxSize'] == 0:
        asg_client.update_auto_scaling_group(AutoScalingGroupName=router_asg_name, MinSize=2, MaxSize=6)


def make_map_from_list(key_in_list_objects, value_in_list_of_objects, list_of_objects):
    converted_map = {}
    for each_obj_in_list in list_of_objects:
        converted_map[each_obj_in_list[key_in_list_objects]] = each_obj_in_list[value_in_list_of_objects]
    return converted_map


def create_app_auto_scaling_group(region_name, automation_bucket_name, war_file_metadata):
    '''
    Here, we know that the app auto scaling group does not exist. so, we find template url for autoscaling group, set
    parameters on it from s3 war file metadata and execute it.
    :return:
    '''
    group_id = war_file_metadata["group_id"]
    artifact_id = war_file_metadata["artifact_id"]
    version = war_file_metadata["version"]
    health_check_url = war_file_metadata["health_check_url"]

    s3_region = '' if region_name == "us-east-1" else '-' + region_name

    cfn_template_s3_url = (
        "https://s3" +
        s3_region +
        ".amazonaws.com/tvarit/default/" +
        plugin_config.plugin_config['groupId'] + "/" + plugin_config.plugin_config['artifactId'] + "/" + plugin_config.plugin_config['version'] +
        "/cloudformation/app/app.template"
    )

    all_top_level_resources = cfn_client.describe_stack_resources(StackName="tvarit-base-infrastructure", LogicalResourceId="Network")
    print(json.dumps(all_top_level_resources, indent=4, sort_keys=True))
    network_resources = cfn_client.describe_stacks(StackName=all_top_level_resources["StackResources"][0]["PhysicalResourceId"])

    map_logical_resource_id_to_logical_resource = make_map_from_list("OutputKey", network_resources["Stacks"][0]["Outputs"])
    # TODO Done till here!
    '''
    We got till here. Need to fix the stack parameters.

    Now we we have all stack outputs for the network stack in a map that is keyed by the OutputKey and value is OutputValue
    '''

    availability_zones = map_logical_resource_id_to_logical_resource["AvailabilityZones"]
    security_groups = map_logical_resource_id_to_logical_resource["SgDefault"]
    app_elb_subnets = map_logical_resource_id_to_logical_resource["AppElbSubnets"]
    app_elb_security_groups = map_logical_resource_id_to_logical_resource["AppSecurityGroups"]
    instance_profile = map_logical_resource_id_to_logical_resource["AppInstanceProfile"]
    cfn_client.create_stack(
        StackName=(group_id + "-" + artifact_id + "-" + version).replace(".", "-"),
        TemplateURL=cfn_template_s3_url,
        Parameters=[
            {
                "ParameterKey": "AppInstanceProfile",
                "ParameterValue": instance_profile
            },
            {
                "ParameterKey": "AvailabilityZones",
                "ParameterValue": availability_zones
            },
            {
                "ParameterKey": "AppSecurityGroup",
                "ParameterValue": security_groups
            },
            {
                "ParameterKey": "AppElbSubnets",
                "ParameterValue": app_elb_subnets
            },
            {
                "ParameterKey": "HealthCheckAbsoluteUrl",
                "ParameterValue": health_check_url
            },
            {
                "ParameterKey": "AppElbSecurityGroup",
                "ParameterValue": app_elb_security_groups
            }
        ]
    )


def modify_router_rules():
    '''
    add the newly created application asg into the router rules.
    '''
    print("do router")


def get_app_metadata(event):
    bucket_name = event["Records"][0]["s3"]["bucket"]["name"]
    key_name = event["Records"][0]["s3"]["object"]["key"]
    all_metadata = s3.head_object(
        Bucket=bucket_name,
        Key=key_name
    )
    return all_metadata


def deploy(event, context):
    '''

        1. Find if router already exists
            2.a. if router doees not exist,
                2.a.i create router.
        3. find if instance for app/version already exists.
            3.a. if version already exists,
                3.a.i. find its template on S3, modify it, create change set and execute
            3.b. if version does not exist,
                3.b.i. find template, copy to local, create
    '''
    print("Starting deploy process")
    all_metadata = get_app_metadata(event)
    ensure_router_auto_scaling_group_has_instances()

    key_of_deployable = event["Records"][0]["s3"]["object"]["key"]
    deployable_name = key_of_deployable.split("/")[1]
    deployable_version = all_metadata['Metadata']['tvarit-app-version']
    tags = asg_client.describe_tags(Filters=[
        {
            "Name": "key",
            "Values": ["tvarit:app:version", "tvarit:app:name"]
        },
        {
            "Name": "value",
            "Values": [deployable_version, deployable_name]
        }
    ])

    if len(tags['Tags']) == 0:
        print("no asg found for " + key_of_deployable + " " + deployable_version)
        create_app_auto_scaling_group(os.environ['AWS_DEFAULT_REGION'], event["Records"][0]["s3"]["bucket"]["name"], all_metadata['Metadata'])

        # TODO Done till here!
        '''
        We got till here. Stack creation for the app is started in method create_app_auto_scaling_group, but it fails because
        none of the parameters are being set correctly. Once that is fixed, we can continue from here on to fix routing rules.
        '''


print(plugin_config.plugin_config)
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
