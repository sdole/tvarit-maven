from __future__ import print_function

import boto3
import uuid

print('Loading function')


def deployNewWar(event, context):
    print("Starting...")
    cfn = boto3.client('cloudformation')
    ec2 = boto3.client('ec2')
    # sgResponse = ec2.describe_security_groups(
    #         Filters=[
    #             {
    #                 'Name': 'tag-key',
    #                 'Values': [
    #                     'tvarit:appsg',
    #                 ]
    #             },
    #         ]
    # )
    # print('done sg')
    # print(sgResponse.get('SecurityGroups')[0].get('GroupId'))
    subnetResponse = ec2.describe_subnets(
            Filters=[
                {
                    'Name': 'tag-key',
                    'Values': [
                        'tvarit:appSubnet'
                    ]
                },
            ]
    )
    subnets = [subnetResponse.get('Subnets')[0].get('SubnetId'),subnetResponse.get('Subnets')[1].get('SubnetId')]
    print(subnets)
    print('done subnet')
    bucketName = event.get('Records')[0].get('s3').get('bucket').get('name')
    print('done subnet')
    print(bucketName)
    print('done subnet')
    objectKey = event.get('Records')[0].get('s3').get('object').get('key')
    print('done subnet')
    print(objectKey)
    print('done subnet')
    print(uuid.uuid4().hex)
    print('done subnet')
    # print(event)
    # for eachSubnet in subnets:

    # cfn.create_stack(
    #         StackName=uuid.uuid4().hex,
    #         TemplateURL='https://s3.amazonaws.com/tvarit-jamesbond-automation/config/autoscaling/newinstance.template',
    #         TimeoutInMinutes=300,
    #         Parameters=[
    #             {
    #                 'ParameterKey': 'tvarit',
    #                 'ParameterValue': 'string',
    #             }, {
    #                 'ParameterKey': 'warFileUrl',
    #                 'ParameterValue': 'string',
    #             }, {
    #                 'ParameterKey': 'tvaritRole',
    #                 'ParameterValue': 'string',
    #             }, {
    #                 'ParameterKey': 'tvaritInstanceProfile',
    #                 'ParameterValue': 'string',
    #             }, {
    #                 'ParameterKey': 'keyName',
    #                 'ParameterValue': 'string',
    #             }, {
    #                 'ParameterKey': 'sgId',
    #                 'ParameterValue': 'string',
    #             }, {
    #                 'ParameterKey': 'bucketName',
    #                 'ParameterValue': 'string',
    #             }, {
    #                 'ParameterKey': 'subnetId',
    #                 'ParameterValue': 'string',
    #             }
    #         ],
    # )