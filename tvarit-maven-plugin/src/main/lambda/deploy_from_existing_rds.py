from __future__ import print_function

import json
import os

import boto3
import datetime

import deploy_utils

json.JSONEncoder.default = lambda self, obj: (obj.isoformat() if isinstance(obj, datetime.datetime) else None)

cfn_client = boto3.client('cloudformation')
s3 = boto3.client('s3')
asg_client = boto3.client('autoscaling')
ec2 = boto3.client('ec2')


def deploy(event, context):
    '''

        1. Find if router already exists
            2.a. if router doees not exist,
                2.a.i create router.
        3. find if instance for app/version already exists.
            3.a. if version already exists,
                3.a.i.  read appsec file if it exists and start all dependencies recursively if they have not
                already been started - this step may need some architecture around sqs and more lambda functions.
                3.a.ii  find its template on S3,
                3.a.ii  modify it, create change set and execute
            3.b. if version does not exist,
                3.b.i.  read appsec file if it exists and start all dependencies recursively if they have not
                already been started - this step may need some architecture around sqs and more lambda functions.
                3.b.ii. find template, create stack
                3.b.iii. add this new version to version rules
    '''
    print("Starting deploy process")
    print("Event is: " + json.dumps(event))
    message_parts = event['Records'][0]['Sns']['Message'].split("\n")
    create_complete = False
    for each_part in message_parts:
        each_part_split = each_part.split("=")
        if each_part_split[0] == "ResourceStatus" and each_part_split[1] == "'CREATE_COMPLETE'":
            create_complete = True
            break
    if not create_complete:
        return
    for each_part in message_parts:
        each_part_split = each_part.split("=")
        if each_part_split[0] == "StackName":
            rds_stack_name = each_part_split[1][1:-1]

    deploy_utils.do_deploy(rds_stack_name)


if __name__ == "__main__":
    os.environ['AWS_DEFAULT_REGION'] = 'us-east-1'
    deploy({
        "Records": [
            {
                "EventVersion": "1.0",
                "EventSubscriptionArn": "arn:aws:sns:us-east-1:085224677438:tvarit-base-infrastructure-SnsTopics-JA32NGGO6G3E-ProvisioningDoneNotificationTopic-YFTWECZVHN5H:f6761cf5-2d2d-426f-adc1-a725dc454568",
                "EventSource": "aws:sns",
                "Sns": {
                    "SignatureVersion": "1",
                    "Timestamp": "2016-09-19T00:15:59.390Z",
                    "Signature": "jUlWCUWGp4pwfh6GRXq9PvK1cyUXIiUTAPFybKJjr8Gm/g2TZNdnpHT6JvsrMjg/PyuSSaETPXGpPTy+r7UMEwwFcfrPtjtpFluc/HT66leeJZyLAv+ekJ1pVNsFdo7SJw320hJqgVuj1BoqX5oyYLzuWuEPdQElQXSnA2TQvVl4PnSEqwSMV/3F0/DZd0Ij8cCAoAUTrV3NlTiUl6dYgkaKdcGuBr1IALEOjIfNvQUSvXb4/A8pG4gJoi1iWUZLWEGFkxMUEKP4WC0pyjyQxljjI9RdorFhiyG0Zvt3IWwhD71C1cytUZLwtVYtpM5lOO1i525NHCnnLLXHS10NxA==",
                    "SigningCertUrl": "https://sns.us-east-1.amazonaws.com/SimpleNotificationService-b95095beb82e8f6a046b3aafc7f4149a.pem",
                    "MessageId": "ac3de2f1-e1a7-5875-bf0a-b2a8d1d8fe1e",
                    "Message": "StackId='arn:aws:cloudformation:us-east-1:085224677438:stack/rdstvarit-tomcat-plugin-test-1-0-1-SNAPSHOT/7a794fa0-7dfc-11e6-8c76-50a686e4bb4a'\nTimestamp='2016-09-19T00:15:58.898Z'\nEventId='AppDB-CREATE_COMPLETE-2016-09-19T00:15:58.898Z'\nLogicalResourceId='AppDB'\nNamespace='085224677438'\nPhysicalResourceId='ravfjph4gdr3qv'\nResourceProperties='{\"MasterUserPassword\":\"tvarit123\",\"DBInstanceClass\":\"db.t2.micro\",\"MasterUsername\":\"tvarit\",\"DBSubnetGroupName\":\"tvarit-base-infrastructure-network-yme8kmvckp6r-dbsubnetgroup-7d8icey2dutq\",\"MultiAZ\":\"false\",\"Engine\":\"postgres\",\"Tags\":[{\"Value\":\"1.0.1\",\"Key\":\"tvarit_version\"},{\"Value\":\"tvarit-tvarit-tomcat-plugin-test::deployables/tvarit/tomcat-plugin-test/1.0.1-SNAPSHOT/tomcat-plugin-test-1.0.1-SNAPSHOT.war\",\"Key\":\"app_file_object\"}],\"AllocatedStorage\":\"50\"}\n'\nResourceStatus='CREATE_COMPLETE'\nResourceStatusReason=''\nResourceType='AWS::RDS::DBInstance'\nStackName='rdstvarit-tomcat-plugin-test-1-0-1-SNAPSHOT'\n",
                    "MessageAttributes": {},
                    "Type": "Notification",
                    "UnsubscribeUrl": "https://sns.us-east-1.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:us-east-1:085224677438:tvarit-base-infrastructure-SnsTopics-JA32NGGO6G3E-ProvisioningDoneNotificationTopic-YFTWECZVHN5H:f6761cf5-2d2d-426f-adc1-a725dc454568",
                    "TopicArn": "arn:aws:sns:us-east-1:085224677438:tvarit-base-infrastructure-SnsTopics-JA32NGGO6G3E-ProvisioningDoneNotificationTopic-YFTWECZVHN5H",
                    "Subject": "AWS CloudFormation Notification"
                }
            }
        ]
    }, {})
