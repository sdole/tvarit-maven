from __future__ import print_function
import boto3

cfn = boto3.client('cloudformation')
s3 = boto3.client('s3')
ec2 = boto3.client('ec2')


def find_app_subnets(projectName):
    logger.debug(projectName)
    subnets = ec2.describe_subnets(
        Filters=[{
            'Name': 'tag-key',
            'Values': [
                projectName + ':appSubnet'
            ]
        }]
    )
    return [subnets['Subnets'][0]['SubnetId'], subnets['Subnets'][1]['SubnetId']]


def find_app_sg(projectName):
    security_groups = ec2.describe_security_groups(
        Filters=[{
            'Name': 'tag-key',
            'Values': [
                projectName + ':appSg'
            ]
        }]
    )
    return security_groups['SecurityGroups'][0]['GroupId']


def find_tvarit_roles(projectName):
    infraStack = cfn.describe_stacks(
        StackName=projectName + '-infra'
    )
    role = infraStack['Stacks'][0]['Outputs'][0]['OutputValue'].split("/")[1]
    instance_profile = infraStack['Stacks'][0]['Outputs'][1]['OutputValue'].split("/")[1]
    return [role, instance_profile]


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
    bucket_name = event["Records"][0]["s3"]["bucket"]["name"]
    keyName = event["Records"][0]["s3"]["object"]["key"]
    s3_object_response = s3.head_object(
        Bucket=bucket_name,
        Key=keyName
    )
    project_name = s3_object_response["Metadata"]["project_name"]
