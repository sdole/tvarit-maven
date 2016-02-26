package com.tvarit.plugin;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.*;
import com.amazonaws.services.ec2.AmazonEC2Client;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "make-infra")
public class InfrastructureMojo extends AbstractMojo {
    @Parameter(required = true, readonly = true, property = "mySecretKey")
    private String secretKey;
    @Parameter(required = true, readonly = true, property = "myAccessKey")
    private String accessKey;
    @Parameter(required = true)
    private String templateUrl;
    @Parameter(required = true)
    private String stackName;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().debug("Starting " + this.getClass().getSimpleName() + " execution ");
        final BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        final AmazonEC2Client amazonEc2Client = new AmazonEC2Client(awsCredentials);
        AmazonCloudFormationClient amazonCloudFormationClient = new AmazonCloudFormationClient(awsCredentials);
        final CreateStackRequest createStackRequest = new CreateStackRequest();
        createStackRequest.withTemplateURL(templateUrl).withStackName(stackName);
        final CreateStackResult stack = amazonCloudFormationClient.createStack(createStackRequest);
        DescribeStacksResult describeStacksResult = amazonCloudFormationClient.describeStacks(new DescribeStacksRequest().withStackName(stackName));
        while (describeStacksResult.getStacks().get(0).getStackStatus().equals(StackStatus.CREATE_IN_PROGRESS.toString())) {
            try {
                getLog().info("Awaiting stack create completion!");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                getLog().error(e);
                throw new RuntimeException(e);
            }
            describeStacksResult = amazonCloudFormationClient.describeStacks(new DescribeStacksRequest().withStackName(stackName));
        }
        getLog().info("Finished completing stack");

    }
}
