from __future__ import print_function

import json
import boto3

print('Loading function')
cfn = boto3.client('cloudformation')


def deployNewWar(event, context):
    print(event)
    print("Starting...")
    allStacksDict = cfn.describe_stacks()
    print(allStacksDict)

