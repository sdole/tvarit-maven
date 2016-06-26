from __future__ import print_function

import boto3

cfn = boto3.client('cloudformation')
s3 = boto3.client('s3')
ec2 = boto3.client('ec2')


def find_app_subnets(projectName):
    print(projectName)
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
