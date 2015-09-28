package com.tvarit.plugin;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;

import java.util.Collections;

/**
 * Created by sachi_000 on 9/21/2015.
 */
public class VpcCreator {
    public VpcIds create(AmazonEC2Client amazonEc2Client, String vpcName) {
        DescribeVpcsRequest describeVpcsRequest = new DescribeVpcsRequest();
        describeVpcsRequest.withFilters(new Filter("tag:Name", Collections.singletonList(vpcName)));
        final DescribeVpcsResult describeVpcsResult = amazonEc2Client.describeVpcs(describeVpcsRequest);
        String vpcId;
        String subnetId;
        if (describeVpcsResult.getVpcs().isEmpty()) {
            CreateVpcRequest createVpcRequest = new CreateVpcRequest();
            createVpcRequest.withCidrBlock("10.0.0.0/16");
            final CreateVpcResult createVpcResult = amazonEc2Client.createVpc(createVpcRequest);
            vpcId = createVpcResult.getVpc().getVpcId();
             CreateTagsRequest createTagsRequest = new CreateTagsRequest();
            createTagsRequest.withResources(vpcId).withTags(new Tag("Name", vpcName));
            amazonEc2Client.createTags(createTagsRequest);
            createTagsRequest = new CreateTagsRequest();
            createTagsRequest.withResources(vpcId).withTags(new Tag("Name", vpcName+"-subnet"));
            amazonEc2Client.createTags(createTagsRequest);
            CreateSubnetRequest createSubnetRequest = new CreateSubnetRequest();
            createSubnetRequest.withVpcId(vpcId).withCidrBlock("10.0.0.0/16");
            final CreateSubnetResult createSubnetResult = amazonEc2Client.createSubnet(createSubnetRequest);
            subnetId = createSubnetResult.getSubnet().getSubnetId();
        } else {
            Vpc vpc = describeVpcsResult.getVpcs().get(0);
            vpcId = vpc.getVpcId();
            DescribeSubnetsRequest describeSubnetsRequest = new DescribeSubnetsRequest();
            describeSubnetsRequest.withFilters(new Filter("tag:Name", Collections.singletonList(vpcName+"-subnet")));
            DescribeSubnetsResult describeSubnetsResult = amazonEc2Client.describeSubnets(describeSubnetsRequest);
            subnetId = describeSubnetsResult.getSubnets().get(0).getSubnetId();
        }
        return new VpcIds(vpcId, subnetId);
    }
}
