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
            final CreateTagsRequest createTagsRequest = new CreateTagsRequest();
            createTagsRequest.withResources(vpcId).withTags(new Tag("Name", vpcName));
            amazonEc2Client.createTags(createTagsRequest);
            CreateSubnetRequest createSubnetRequest = new CreateSubnetRequest();
            createSubnetRequest.withVpcId(vpcId);
            final CreateSubnetResult createSubnetResult = amazonEc2Client.createSubnet(createSubnetRequest);
            subnetId = createSubnetResult.getSubnet().getSubnetId();
        } else {
            vpcId = describeVpcsResult.getVpcs().get(0).getVpcId();
//            subnetId = describeVpcsResult.getVpcs().get(0).get
        }
        return new VpcIds(vpcId, subnetId);
    }
}
