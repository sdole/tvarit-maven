from __future__ import print_function

import json
import os

import boto3
import datetime

import util

json.JSONEncoder.default = lambda self, obj: (obj.isoformat() if isinstance(obj, datetime.datetime) else None)

cfn_client = boto3.client('cloudformation')
s3 = boto3.client('s3')
asg_client = boto3.client('autoscaling')
ec2 = boto3.client('ec2')


def find_template():
    print("in find template")


def modify_template():
    print("in modify template")


def execute_stack():
    print("in execute stack")


def do_instances_exist(app_instance_tag):
    instances = ec2.describe_instances(Filters=[{'Name': 'tag-key', 'Values': [app_instance_tag]}])
    return len(instances['Reservations'][0]['Instances']) > 0


def do_router_exists():
    tags = asg_client.describe_tags(Filters=[
        {
            "Name": "key",
            "Values": ["tvarit:purpose"]
        },
        {
            "Name": "value",
            "Values": ["router"]
        }
    ])

    instances = ec2.describe_instances(Filters=[{'Name': 'tag:key', 'Values': ["tvarit:purpose=router"]}])
    print(json.dumps(instances))

    return len(instances['Reservations']) > 0 and len(instances['Reservations'][0]['Instances']) > 0


def ensure_router_auto_scaling_group_has_instances():
    tags = asg_client.describe_tags(Filters=[
        {
            "Name": "key",
            "Values": ["tvarit:purpose"]
        },
        {
            "Name": "value",
            "Values": ["router"]
        }
    ])

    router_asg_name = tags['Tags'][0]['ResourceId']

    auto_scaling_groups = asg_client.describe_auto_scaling_groups(AutoScalingGroupNames=[router_asg_name])
    if auto_scaling_groups['AutoScalingGroups'][0]['MaxSize'] == 0:
        asg_client.update_auto_scaling_group(AutoScalingGroupName=router_asg_name, MinSize=2, MaxSize=6)


def create_app_auto_scaling_group(war_file_metadata):
    '''
    Here, we know that the app auto scaling group does not exist. so, we find template url for app autoscaling group, set
    parameters on it from s3 war file metadata and execute it.
    :return:
    '''
    group_id = war_file_metadata["group-id"]
    artifact_id = war_file_metadata["artifact-id"]
    version = war_file_metadata["version"]
    health_check_url = war_file_metadata["health_check_url"]

    print(json.dumps(war_file_metadata, indent=True, sort_keys=True))

    cfn_template_s3_url = util.make_cfn_url("app/app.template")
    network_resources = util.make_resources_map_from_cfn("Network")
    iam_resources = util.make_resources_map_from_cfn("IAM")

    availability_zones = network_resources["AvailabilityZonesOutput"]
    app_security_groups = network_resources["AppSecurityGroupsOutput"]
    elb_subnets = network_resources["ElbSubnetsOutput"]
    app_subnets = network_resources["AppSubnetsOutput"]
    app_elb_security_groups = network_resources["ElbSecurityGroupsOutput"]
    instance_profile = iam_resources["AppInstanceProfileOutput"]
    app_stack_parameters = [
        {"ParameterKey": "AppSubnetsParam", "ParameterValue": app_subnets},
        {"ParameterKey": "AppInstanceProfileParam", "ParameterValue": instance_profile},
        {"ParameterKey": "AvailabilityZonesParam", "ParameterValue": availability_zones},
        {"ParameterKey": "AppSecurityGroupParam", "ParameterValue": app_security_groups},
        {"ParameterKey": "ElbSubnetsParam", "ParameterValue": elb_subnets},
        {"ParameterKey": "HealthCheckUrlParam", "ParameterValue": health_check_url},
        {"ParameterKey": "ElbSecurityGroupParam", "ParameterValue": app_elb_security_groups}
    ]
    cfn_client.create_stack(
        StackName=(group_id + "-" + artifact_id + "-" + version).replace(".", "-"),
        TemplateURL=cfn_template_s3_url,
        Parameters=app_stack_parameters
    )


def modify_router_rules():
    '''
    add the newly created application asg into the router rules.
    '''
    print("do router")


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
        print("--"+each_part_split[0]+"--")
        # print("--"+each_part_split[1]+"--")
        if each_part_split[0] == "ResourceStatus" and each_part_split[1] == "'CREATE_COMPLETE'":
            create_complete = True
            break
        else:
            print("no")
    if not create_complete:
        return
    for each_part in message_parts:
        each_part_split = each_part.split("=")
        if each_part_split[0] == "StackName":
            rds_stack_name = each_part_split[1][1:-1]

    rds_stack = cfn_client.describe_stacks(StackName=rds_stack_name)
    tags_on_rds_stack = rds_stack['Stacks'][0]['Tags']
    map_of_tags_on_rds_stack = util.make_map_from_list("Key", "Value", tags_on_rds_stack)
    app_file_object_parm = map_of_tags_on_rds_stack['app_file_object']
    bucket_name = app_file_object_parm.split("::")[0]
    key_of_deployable = app_file_object_parm.split("::")[1]
    # util.get_app_metadata(bucket_name)
    # bucket_name = event["Records"][0]["s3"]["bucket"]["name"]
    # key_of_deployable = event["Records"][0]["s3"]["object"]["key"]
    all_metadata = util.get_app_metadata(bucket_name, key_of_deployable)
    ensure_router_auto_scaling_group_has_instances()

    deployable_name = key_of_deployable.split("/")[1]
    deployable_version = all_metadata['Metadata']['version']
    tags = asg_client.describe_tags(Filters=[
        {
            "Name": "key",
            "Values": ["tvarit:app:version", "tvarit:app:name"]
        },
        {
            "Name": "value",
            "Values": [deployable_version, deployable_name]
        }
    ])

    if len(tags['Tags']) == 0:
        print("no asg found for " + key_of_deployable + " " + deployable_version)
        create_app_auto_scaling_group(
            all_metadata['Metadata']
        )
        # TODO Done till here!
        '''
        Stack creation for the app is started in method create_app_auto_scaling_group,
        continue from here on to modify routing rules.
        '''

        modify_router_rules()

    else:
        # TODO
        '''
        at this point, it is known that there already is a version of the same app. The developer's
        probable intention was to replace the running code with new code for the same version. So,
        find the app template in S3, change the logical name for the launch config, save it to
        another folder named revisions within the same version name folder and then execute that
        changeset. This step might also create new dependency versions. If so, create stacks for
        all those dependencues.
        '''


if __name__ == "__main__":
    os.environ['AWS_DEFAULT_REGION'] = 'us-east-1'
    deploy({
        "Records": [
            {
                "EventVersion": "1.0",
                "EventSubscriptionArn": "arn:aws:sns:us-east-1:085224677438:tvarit-base-infrastructure2-SnsTopics-JA32NGGO6G3E-ProvisioningDoneNotificationTopic-YFTWECZVHN5H:f6761cf5-2d2d-426f-adc1-a725dc454568",
                "EventSource": "aws:sns",
                "Sns": {
                    "SignatureVersion": "1",
                    "Timestamp": "2016-09-19T00:15:59.390Z",
                    "Signature": "jUlWCUWGp4pwfh6GRXq9PvK1cyUXIiUTAPFybKJjr8Gm/g2TZNdnpHT6JvsrMjg/PyuSSaETPXGpPTy+r7UMEwwFcfrPtjtpFluc/HT66leeJZyLAv+ekJ1pVNsFdo7SJw320hJqgVuj1BoqX5oyYLzuWuEPdQElQXSnA2TQvVl4PnSEqwSMV/3F0/DZd0Ij8cCAoAUTrV3NlTiUl6dYgkaKdcGuBr1IALEOjIfNvQUSvXb4/A8pG4gJoi1iWUZLWEGFkxMUEKP4WC0pyjyQxljjI9RdorFhiyG0Zvt3IWwhD71C1cytUZLwtVYtpM5lOO1i525NHCnnLLXHS10NxA==",
                    "SigningCertUrl": "https://sns.us-east-1.amazonaws.com/SimpleNotificationService-b95095beb82e8f6a046b3aafc7f4149a.pem",
                    "MessageId": "ac3de2f1-e1a7-5875-bf0a-b2a8d1d8fe1e",
                    "Message": "StackId='arn:aws:cloudformation:us-east-1:085224677438:stack/rdstvarit-tomcat-plugin-test-1-0-1-SNAPSHOT/7a794fa0-7dfc-11e6-8c76-50a686e4bb4a'\nTimestamp='2016-09-19T00:15:58.898Z'\nEventId='AppDB-CREATE_COMPLETE-2016-09-19T00:15:58.898Z'\nLogicalResourceId='AppDB'\nNamespace='085224677438'\nPhysicalResourceId='ravfjph4gdr3qv'\nResourceProperties='{\"MasterUserPassword\":\"tvarit123\",\"DBInstanceClass\":\"db.t2.micro\",\"MasterUsername\":\"tvarit\",\"DBSubnetGroupName\":\"tvarit-base-infrastructure2-network-yme8kmvckp6r-dbsubnetgroup-7d8icey2dutq\",\"MultiAZ\":\"false\",\"Engine\":\"postgres\",\"Tags\":[{\"Value\":\"1.0.1\",\"Key\":\"tvarit_version\"},{\"Value\":\"tvarit-tvarit-tomcat-plugin-test::deployables/tvarit/tomcat-plugin-test/1.0.1-SNAPSHOT/tomcat-plugin-test-1.0.1-SNAPSHOT.war\",\"Key\":\"app_file_object\"}],\"AllocatedStorage\":\"50\"}\n'\nResourceStatus='CREATE_COMPLETE'\nResourceStatusReason=''\nResourceType='AWS::RDS::DBInstance'\nStackName='rdstvarit-tomcat-plugin-test-1-0-1-SNAPSHOT'\n",
                    "MessageAttributes": {},
                    "Type": "Notification",
                    "UnsubscribeUrl": "https://sns.us-east-1.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:us-east-1:085224677438:tvarit-base-infrastructure2-SnsTopics-JA32NGGO6G3E-ProvisioningDoneNotificationTopic-YFTWECZVHN5H:f6761cf5-2d2d-426f-adc1-a725dc454568",
                    "TopicArn": "arn:aws:sns:us-east-1:085224677438:tvarit-base-infrastructure2-SnsTopics-JA32NGGO6G3E-ProvisioningDoneNotificationTopic-YFTWECZVHN5H",
                    "Subject": "AWS CloudFormation Notification"
                }
            }
        ]
    }, {})
