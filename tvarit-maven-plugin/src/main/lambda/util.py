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


def make_s3_base_url():
    region_name = os.environ['AWS_DEFAULT_REGION']
    s3_region = '' if region_name == "us-east-1" else '-' + region_name
    s3_base_url = "https://s3" + s3_region + ".amazonaws.com"
    return s3_base_url


def make_cfn_url(template_file):
    return (
        make_s3_base_url() +
        "/tvarit/default/" +
        plugin_config.plugin_config['groupId'] +
        "/" +
        plugin_config.plugin_config['artifactId'] +
        "/" +
        plugin_config.plugin_config['version'] +
        "/cloudformation/" +
        template_file
    )


def make_base_output_map_from_cfn(sub_stack_name):
    base_stack_resources = cfn_client.describe_stack_resources(StackName=constants.BASE_STACK_NAME, LogicalResourceId=sub_stack_name)
    sub_stack_id = base_stack_resources["StackResources"][0]["PhysicalResourceId"]
    return make_stack_output_map(sub_stack_id)


def make_stack_output_map(stack_name):
    stack_info = cfn_client.describe_stacks(StackName=stack_name)
    outputs_map = make_map_from_list("OutputKey", "OutputValue", stack_info["Stacks"][0]["Outputs"])
    return outputs_map


def make_map_from_list(key_in_list_objects, value_in_list_of_objects, list_of_objects):
    converted_map = {}
    for each_obj_in_list in list_of_objects:
        converted_map[each_obj_in_list[key_in_list_objects]] = each_obj_in_list[value_in_list_of_objects]
    return converted_map
