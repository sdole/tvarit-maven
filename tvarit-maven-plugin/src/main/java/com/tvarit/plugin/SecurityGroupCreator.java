package com.tvarit.plugin;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;

/**
 * Created by sachi_000 on 10/13/2015.
 */
public class SecurityGroupCreator {
    public String create(AmazonEC2Client amazonEC2Client, String vpcId, String baseName) {
        final CreateSecurityGroupRequest createSecurityGroupRequest = new CreateSecurityGroupRequest();
        final String groupName = baseName + "-securityGroup";
        createSecurityGroupRequest.
                withVpcId(vpcId).
                withGroupName(groupName).
                withDescription("Tvarit securityGroup")
        ;
        final CreateSecurityGroupResult createSecurityGroupResult = amazonEC2Client.createSecurityGroup(createSecurityGroupRequest);
        final AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest = new AuthorizeSecurityGroupIngressRequest();
        final String groupId = createSecurityGroupResult.getGroupId();
        authorizeSecurityGroupIngressRequest
                .withGroupId(groupId)
                .withCidrIp("0.0.0.0/0")
                .withIpProtocol("tcp")
                .withFromPort(8080)
                .withToPort(8080)
        ;
        amazonEC2Client
                .authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest)
        ;
        final AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest2 = new AuthorizeSecurityGroupIngressRequest();
        authorizeSecurityGroupIngressRequest2
                .withGroupId(groupId)
                .withCidrIp("0.0.0.0/0")
                .withIpProtocol("tcp")
                .withFromPort(9990)
                .withToPort(9990)
        ;
        amazonEC2Client
                .authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest2)
        ;
        return groupId;
    }
}
