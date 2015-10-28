package com.tvarit.plugin;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;

import java.util.Collections;

/**
 * Created by sachi_000 on 9/21/2015.
 */
public class VpcCreator {
    public String create(AmazonEC2Client amazonEc2Client, String baseName) {
        baseName = baseName + "-vpc";
        DescribeVpcsRequest describeVpcsRequest = new DescribeVpcsRequest();
        describeVpcsRequest.withFilters(new Filter("tag:Name", Collections.singletonList(baseName)));
        final DescribeVpcsResult describeVpcsResult = amazonEc2Client.describeVpcs(describeVpcsRequest);
        String vpcId;
        if (describeVpcsResult.getVpcs().isEmpty()) {
            CreateVpcRequest createVpcRequest = new CreateVpcRequest();
            createVpcRequest.withCidrBlock("10.0.0.0/16");
            final CreateVpcResult createVpcResult = amazonEc2Client.createVpc(createVpcRequest);
            vpcId = createVpcResult.getVpc().getVpcId();
            CreateTagsRequest createTagsRequest = new CreateTagsRequest();
            createTagsRequest.withResources(vpcId).withTags(new Tag("Name", baseName));
            amazonEc2Client.createTags(createTagsRequest);
        } else {
            Vpc vpc = describeVpcsResult.getVpcs().get(0);
            vpcId = vpc.getVpcId();
        }
        return vpcId;
    }
}
