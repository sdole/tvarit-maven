from __future__ import print_function

import boto3
import logging

logger = logging.getLogger("Tvarit")
logger.setLevel(logging.DEBUG)
formatter = logging.Formatter('[%(levelname)s] : %(filename)s.%(funcName)s : %(message)s')
handler = logging.StreamHandler()
handler.setFormatter(formatter)
logger.addHandler(handler)



logger.debug('Loading function')
cfn = boto3.client('cloudformation')
s3 = boto3.client('s3')
ec2Client = boto3.client('ec2')



def find_app_subnets(projectName):
    logger.debug(projectName)
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
    logger.debug(security_groups['SecurityGroups'][0]['GroupId'])
    return security_groups['SecurityGroups'][0]['GroupId']

def find_tvarit_roles(projectName):
    infraStack = cfn.describe_stacks(
        StackName=projectName+'-infra'
    )
    logger.debug(infraStack['Stacks'][0])
    role = infraStack['Stacks'][0]['Outputs'][0]['OutputValue'].split("/")[1]
    instance_profile = infraStack['Stacks'][0]['Outputs'][1]['OutputValue'].split("/")[1]
    logger.debug(role)
    logger.debug(instance_profile)
    return [role, instance_profile]


def deployNewWar(event, context):
    logger.debug(event)
    logger.debug("Starting...")

    bucketName = event["Records"][0]["s3"]["bucket"]["name"]
    keyName = event["Records"][0]["s3"]["object"]["key"]

    logger.debug(bucketName)
    logger.debug(keyName)
    s3_object_response = s3.head_object(
        Bucket=bucketName,
        Key=keyName
    )
    logger.debug(s3_object_response)
    logger.debug(s3_object_response["Metadata"])
    project_name = s3_object_response["Metadata"]["project_name"]
    stack_template_url = s3_object_response["Metadata"]["stack_template_url"]
    private_key_name = s3_object_response["Metadata"]["private_key_name"]
    logger.debug("project name: " + project_name)
    logger.debug("stack_template_url" + stack_template_url)
    war_file_url = "https://s3.amazonaws.com/"+bucketName+"/"+keyName
    logger.debug(war_file_url)
    subnets = find_app_subnets(project_name)
    logger.debug(subnets)
    create_new_instance_response = cfn.create_stack(
        StackName=project_name+"-new-instance",
        TemplateURL=stack_template_url,
        Parameters=[
             {
                'ParameterKey': 'publicSubnets',
                'ParameterValue': subnets[0]+","+subnets[1]
            },
            {
                'ParameterKey': 'tvaritRole',
                'ParameterValue': find_tvarit_roles(project_name)[0]
            },
            {
                'ParameterKey': 'tvaritInstanceProfile',
                'ParameterValue': find_tvarit_roles(project_name)[1]
            },
            {
                'ParameterKey': 'bucketName',
                'ParameterValue': bucketName
            },
            {
                'ParameterKey': 'sgId',
                'ParameterValue': find_app_sg(project_name)
            },
            {
                'ParameterKey': 'keyName',
                'ParameterValue': private_key_name
            },
            {
                'ParameterKey': 'warFileUrl',
                'ParameterValue': war_file_url
            }
        ],
        DisableRollback=True,
        TimeoutInMinutes=10
    )

    logger.debug(create_new_instance_response)

