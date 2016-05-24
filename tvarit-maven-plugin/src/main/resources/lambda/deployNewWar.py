from __future__ import print_function

import boto3

print('Loading function')
cfn = boto3.client('cloudformation')
s3 = boto3.client('s3')
ec2Client = boto3.client('ec2')



def find_app_subnets(projectName):
    print(projectName)
    subnets = ec2Client.describe_subnets(
        Filters=[{
            'Name': 'tag-key',
            'Values': [
                projectName + ':appSubnet'
            ]
        }]
    )
    return [subnets['Subnets'][0]['SubnetId'],subnets['Subnets'][1]['SubnetId']]

def find_app_sg(projectName):
    security_groups = ec2Client.describe_security_groups(
        Filters=[{
            'Name': 'tag-key',
            'Values': [
                projectName + ':appSg'
            ]
        }]
    )
    print(security_groups['SecurityGroups'][0]['GroupId'])
    return security_groups['SecurityGroups'][0]['GroupId']

def find_tvarit_roles(projectName):
    infraStack = cfn.describe_stacks(
        StackName=projectName+'-infra'
    )
    print(infraStack['Stacks'][0])
    return [infraStack['Stacks'][0]['Outputs'][0]['OutputValue'],infraStack['Stacks'][0]['Outputs'][1]['OutputValue']]


def deployNewWar(event, context):
    print(event)
    print("Starting...")

    bucketName = event["Records"][0]["s3"]["bucket"]["name"]
    keyName = event["Records"][0]["s3"]["object"]["key"]

    print(bucketName)
    print(keyName)
    s3ObjectResponse = s3.head_object(
        Bucket=bucketName,
        Key=keyName
    )
    print(s3ObjectResponse)
    print(s3ObjectResponse["Metadata"])
    projectName = s3ObjectResponse["Metadata"]["project_name"]
    stackTemplateUrl = s3ObjectResponse["Metadata"]["stack_template_url"]
    private_key_name = s3ObjectResponse["Metadata"]["private_key_name"]
    print("project name: " + projectName)
    print("stack_template_url" + stackTemplateUrl)
    warFileUrl = "https://s3.amazonaws.com/"+bucketName+"/"+keyName
    print(warFileUrl)
    subnets = find_app_subnets(projectName)
    print (subnets)
    createNewInstanceResponse = cfn.create_stack(
        StackName=projectName+"-new-instance",
        TemplateURL=stackTemplateUrl,
        Parameters=[
             {
                'ParameterKey': 'publicSubnets',
                'ParameterValue': subnets[0]+","+subnets[1]
            },
            {
                'ParameterKey': 'tvaritRole',
                'ParameterValue': find_tvarit_roles(projectName)[0]
            },
            {
                'ParameterKey': 'tvaritInstanceProfile',
                'ParameterValue': find_tvarit_roles(projectName)[1]
            },
            {
                'ParameterKey': 'bucketName',
                'ParameterValue': bucketName
            },
            {
                'ParameterKey': 'sgId',
                'ParameterValue': find_app_sg(projectName)
            },
            {
                'ParameterKey': 'keyName',
                'ParameterValue': private_key_name
            },
            {
                'ParameterKey': 'warFileUrl',
                'ParameterValue': warFileUrl
            }
        ],
        DisableRollback=True,
        TimeoutInMinutes=10
    )

    print(createNewInstanceResponse)

find_tvarit_roles("testFive")