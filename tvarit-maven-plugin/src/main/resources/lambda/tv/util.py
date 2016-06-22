from __future__ import print_function


def find_autoscaling_group(group_id, artifact_id, version):
    aws_session = session.AwsSession()
    ec2_client = aws_session.getattr("ec2_client")
    volumes = ec2_client.describe_volumes()
    print(volumes)

if __name__=="__main__":
    find_autoscaling_group(None,None,None)
