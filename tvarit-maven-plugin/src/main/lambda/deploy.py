from __future__ import print_function

import boto3

cfn = boto3.client('cloudformation')
s3 = boto3.client('s3')
autoscaling = boto3.client('autoscaling')
ec2 = boto3.client('ec2')


def find_template():
    pass


def modify_template():
    pass


def execute_stack():
    pass


def do_instances_exist(app_instance_tag):
    instances = ec2.describe_instances(Filters=[{'Name': 'tag-key', 'Values': [app_instance_tag]}])
    return len(instances['Reservations'][0]['Instances']) > 0


def do_router_exists():
    instances = ec2.describe_instances(Filters=[{'Name': 'tag-key', 'Values': ["tvarit::router"]}])
    return len(instances['Reservations'][0]['Instances']) > 0


def create_router():
    '''
    TODO describe stack that has the reverse proxy, find the autoscaling group in it. We have to do this because, when an autoscaling group is created in a cf stack, we cannnot specify a specific
    name. it is created dynamically by cloudformation. so, we need to dump the name in the stack outputs - TBD and then query that over here. then, we increment the instance counts in that asg to 2.
    '''
    cfn.describe_stacks("")
    autoscaling.describe_auto_scaling_groups(AutoScalingGroupNames=["ReverseProxyRouter"])
    pass


def create_autoscaling_group():
    pass


def modify_router_rules():
    pass


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
    app_instance_tag = get_app_metadata(event)
    if not do_instances_exist(app_instance_tag):
        if not do_router_exists():
            create_router()
        create_autoscaling_group()
        modify_router_rules()
    else:
        find_template()  # find the template that is used to start the autoscaling group for this app/version
        modify_template()  # copy the launch config into a variable, modify the launch config logical name
        execute_stack()  # execute the new stack from memory (no need to save it in S3)


def get_app_metadata(event):
    bucket_name = event["Records"][0]["s3"]["bucket"]["name"]
    keyName = event["Records"][0]["s3"]["object"]["key"]
    s3_object_response = s3.head_object(
        Bucket=bucket_name,
        Key=keyName
    )
    key_name_split = keyName.split("/")
    return "tvarit::" + key_name_split[1] + "::" + key_name_split[2] + "::" + key_name_split[3]
