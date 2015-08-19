package com.tvarit.plugin;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.opsworks.AWSOpsWorksClient;
import com.amazonaws.services.opsworks.model.*;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;
import java.util.stream.Collectors;

@Mojo(name = "hello", defaultPhase = LifecyclePhase.COMPILE)
public class TvaritMojo extends AbstractMojo {

    @Parameter(required = true, alias = "stack")
    private String stackName;

    @Parameter(required = true, alias = "accessKey")
    private String accessKey;
    @Parameter(required = true, alias = "secretKey",readonly = true)
    private String secretKey;
    @Parameter(required = true,alias = "role_arn")
    private String roleArn;
    @Parameter(required = true,alias="instance_profile_arn")
    private String instanceProfileArn;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().debug("Starting " + this.getClass().getSimpleName() + " execution ");
        AWSOpsWorksClient awsOpsWorksClient = new AWSOpsWorksClient(new BasicAWSCredentials(accessKey, secretKey));
        DescribeStacksRequest describeStacksRequest = new DescribeStacksRequest();
        DescribeStacksResult describeStacksResult = awsOpsWorksClient.describeStacks(describeStacksRequest);
        List<String> stacksFound = describeStacksResult.getStacks().stream().map(Stack::getName).collect(Collectors.toList());
        if (stacksFound.isEmpty() || !stacksFound.contains(stackName)) {
            getLog().debug("No stacks found! Will create!");
            CreateStackRequest createStackRequest = new CreateStackRequest();
            createStackRequest.withName(stackName).withServiceRoleArn(roleArn).withRegion(Regions.US_EAST_1.getName()).withDefaultInstanceProfileArn(instanceProfileArn);
            CreateStackResult createStackResult = awsOpsWorksClient.createStack(createStackRequest);
            getLog().debug("Created stack! "  + createStackResult.getStackId());
        } else {
            getLog().debug("Found stacks: " + stacksFound.toString());
        }
        getLog().debug("Done!");
    }

}
