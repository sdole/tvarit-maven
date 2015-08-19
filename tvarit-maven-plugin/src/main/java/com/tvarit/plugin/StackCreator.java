package com.tvarit.plugin;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.opsworks.AWSOpsWorksClient;
import com.amazonaws.services.opsworks.model.*;

import java.util.List;
import java.util.stream.Collectors;

public class StackCreator  {
    public String create(AWSOpsWorksClient awsOpsWorksClient, String stackName, TvaritMojo tvaritMojo, String roleArn, String layerName, String instanceProfileArn) {
        DescribeStacksRequest describeStacksRequest = new DescribeStacksRequest();
        DescribeStacksResult describeStacksResult = awsOpsWorksClient.describeStacks(describeStacksRequest);
        List<String> stacksFound = describeStacksResult.getStacks().stream().map(Stack::getName).collect(Collectors.toList());
        if (stacksFound.isEmpty() || !stacksFound.contains(stackName)) {
            tvaritMojo.getLog().debug("No stacks found! Will create!");
            CreateStackRequest createStackRequest = new CreateStackRequest();
            createStackRequest.withName(stackName).withServiceRoleArn(roleArn).withRegion(Regions.US_EAST_1.getName()).withDefaultInstanceProfileArn(instanceProfileArn);
            CreateStackResult createStackResult = awsOpsWorksClient.createStack(createStackRequest);
            tvaritMojo.getLog().debug("Created stack! "  + createStackResult.getStackId());
            return createStackResult.getStackId();
        } else {
            tvaritMojo.getLog().debug("Found stacks: " + stacksFound.toString());
            return describeStacksResult.getStacks().get(0).getStackId();
        }
    }
}
