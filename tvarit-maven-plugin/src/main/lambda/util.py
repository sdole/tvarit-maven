import os

import boto3

import constants
import plugin_config

cfn_client = boto3.client('cloudformation')

s3 = boto3.client('s3')


def get_app_metadata(bucket_name, key_name):
    all_metadata = s3.head_object(
        Bucket=bucket_name,
        Key=key_name
    )
    return all_metadata


def make_cfn_url(template_file):
    region_name = os.environ['AWS_DEFAULT_REGION']
    s3_region = '' if region_name == "us-east-1" else '-' + region_name
    cfn_template_s3_url = (
        "https://s3" +
        s3_region +
        ".amazonaws.com/tvarit/default/" +
        plugin_config.plugin_config['groupId'] + "/" + plugin_config.plugin_config['artifactId'] + "/" + plugin_config.plugin_config['version'] +
        "/cloudformation/" +
        template_file
    )
    return cfn_template_s3_url


def make_resources_map_from_cfn(sub_stack_name):
    network_stack_info = cfn_client.describe_stack_resources(StackName=constants.BASE_STACK_NAME, LogicalResourceId=sub_stack_name)
    network_resources = cfn_client.describe_stacks(StackName=network_stack_info["StackResources"][0]["PhysicalResourceId"])
    network_resources = make_map_from_list("OutputKey", "OutputValue", network_resources["Stacks"][0]["Outputs"])
    return network_resources


def make_map_from_list(key_in_list_objects, value_in_list_of_objects, list_of_objects):
    converted_map = {}
    for each_obj_in_list in list_of_objects:
        converted_map[each_obj_in_list[key_in_list_objects]] = each_obj_in_list[value_in_list_of_objects]
    return converted_map
