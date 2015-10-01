package com.tvarit.plugin;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.opsworks.AWSOpsWorksClient;
import com.amazonaws.services.opsworks.model.*;

import java.util.List;
import java.util.stream.Collectors;

public class StackCreator  {
    public String create(AWSOpsWorksClient awsOpsWorksClient, String stackName, TvaritMojo tvaritMojo, String roleArn, String layerName, String instanceProfileArn, String vpcId,String subnetId) {
        DescribeStacksRequest describeStacksRequest = new DescribeStacksRequest();
        DescribeStacksResult describeStacksResult = awsOpsWorksClient.describeStacks(describeStacksRequest);
        List<String> stacksFound = describeStacksResult.getStacks().stream().map(Stack::getName).collect(Collectors.toList());
        if (stacksFound.isEmpty() || !stacksFound.contains(stackName)) {
            tvaritMojo.getLog().debug("No stacks found! Will create!");
            CreateStackRequest createStackRequest = new CreateStackRequest();
            final Source customCookbooksSource = new Source();
            customCookbooksSource.setUrl("https://github.com/sdole/tvarit-2");
            customCookbooksSource.setType(SourceType.Git);
            final ChefConfiguration chefConfiguration = new ChefConfiguration();
            chefConfiguration.withManageBerkshelf(true);
            final StackConfigurationManager stackConfigurationManager = new StackConfigurationManager();
            stackConfigurationManager.withVersion("11.4");
            createStackRequest.
                    withName(stackName).
                    withServiceRoleArn(roleArn).
                    withRegion(Regions.US_EAST_1.getName()).
                    withDefaultInstanceProfileArn(instanceProfileArn).
                    withCustomCookbooksSource(customCookbooksSource).
                    withUseCustomCookbooks(true).
                    withVpcId(vpcId).
                    withDefaultSubnetId(subnetId).
                    withChefConfiguration(chefConfiguration).
                    withConfigurationManager(stackConfigurationManager);
            CreateStackResult createStackResult = awsOpsWorksClient.createStack(createStackRequest);
            tvaritMojo.getLog().debug("Created stack! "  + createStackResult.getStackId());
            return createStackResult.getStackId();
        } else {
            tvaritMojo.getLog().debug("Found stacks: " + stacksFound.toString());
            return describeStacksResult.getStacks().get(0).getStackId();
        }
    }
}
