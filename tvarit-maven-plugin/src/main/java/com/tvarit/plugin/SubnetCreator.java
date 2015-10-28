package com.tvarit.plugin;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;

import java.util.Collections;

/**
 * Created by sachi_000 on 9/28/2015.
 */
public class SubnetCreator {
    String create(AmazonEC2Client amazonEc2Client, String vpcId, String baseName) {

        String subnetName = baseName+"-subnet";
        DescribeSubnetsRequest describeSubnetsRequest = new DescribeSubnetsRequest();
        describeSubnetsRequest.withFilters(new Filter("tag:Name", Collections.singletonList(subnetName)));
        DescribeSubnetsResult describeSubnetsResult = amazonEc2Client.describeSubnets(describeSubnetsRequest);
        String subnetId;
        if (describeSubnetsResult.getSubnets().isEmpty()) {
            CreateSubnetRequest createSubnetRequest = new CreateSubnetRequest();
            createSubnetRequest.withVpcId(vpcId).withCidrBlock("10.0.0.0/16");
            final CreateSubnetResult createSubnetResult = amazonEc2Client.createSubnet(createSubnetRequest);
            subnetId = createSubnetResult.getSubnet().getSubnetId();
            CreateTagsRequest createTagsRequest = new CreateTagsRequest();
            createTagsRequest.withResources(subnetId).withTags(new Tag("Name", subnetName));
            amazonEc2Client.createTags(createTagsRequest);
        } else {
            subnetId = describeSubnetsResult.getSubnets().get(0).getSubnetId();
        }

        return subnetId;
    }

}
