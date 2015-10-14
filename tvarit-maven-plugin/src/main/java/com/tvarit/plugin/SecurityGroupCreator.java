package com.tvarit.plugin;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.IpPermission;

/**
 * Created by sachi_000 on 10/13/2015.
 * @deprecated dont really need this!
 */
@Deprecated
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
        final IpPermission letHttpIn = new IpPermission();
        letHttpIn.withIpProtocol("tcp").withFromPort(80).withToPort(80);
        final IpPermission letSshIn = new IpPermission();
        letSshIn.withIpProtocol("tcp").withFromPort(80).withToPort(80);
        final AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest = new AuthorizeSecurityGroupIngressRequest();
        final String groupId = createSecurityGroupResult.getGroupId();
        authorizeSecurityGroupIngressRequest.
                withGroupId(groupId).
                withIpPermissions(letSshIn, letHttpIn).
                withCidrIp("0.0.0.0/0")
        ;
        amazonEC2Client.authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);
        return groupId;
    }
}
