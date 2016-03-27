from __future__ import print_function

import boto3

print('Loading function')
cfn = boto3.client('cloudformation')
s3 = boto3.client('s3')


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
    # createNewInstanceResponse = cfn.create_stack(
    #     StackName=projectName+"new-instance",
    #     TemplateURL=stackTemplateUrl,
    #     Parameters=[
    #          {
    #             'ParameterKey': 'publicSubnets',
    #             'ParameterValue': 'string'
    #         },
    #         {
    #             'ParameterKey': 'tvaritRole',
    #             'ParameterValue': 'string'
    #         },
    #         {
    #             'ParameterKey': 'tvaritInstanceProfile',
    #             'ParameterValue': 'string'
    #         },
    #         {
    #             'ParameterKey': 'bucketName',
    #             'ParameterValue': bucketName
    #         },
    #         {
    #             'ParameterKey': 'sgId',
    #             'ParameterValue': 'string'
    #         },
    #         {
    #             'ParameterKey': 'keyName',
    #             'ParameterValue': private_key_name
    #         },
    #         {
    #             'ParameterKey': 'warFileUrl',
    #             'ParameterValue': warFileUrl
    #         }
    #     ],
    #     DisableRollback=True,
    #     TimeoutInMinutes=10
    # )
    #
    # print(createNewInstanceResponse)


