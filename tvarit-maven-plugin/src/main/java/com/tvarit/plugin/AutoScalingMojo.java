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

@Mojo(name = "setup-autoscaling")
public class AutoScalingMojo extends AbstractMojo {

    @Parameter(required = true, readonly = true, property = "mySecretKey")
    private String secretKey;
    @Parameter(required = true, readonly = true, property = "myAccessKey")
    private String accessKey;
    @Parameter
    private String templateUrl;
    @Parameter(required = true)
    private String projectName;
    @Parameter(required = true)
    private String domainName;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().debug("Starting " + this.getClass().getSimpleName() + " execution ");
        final BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        final AmazonEC2Client amazonEc2Client = new AmazonEC2Client(awsCredentials);
        AmazonCloudFormationClient amazonCloudFormationClient = new AmazonCloudFormationClient(awsCredentials);
        final com.amazonaws.services.cloudformation.model.Parameter domainNameParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("domainName").withParameterValue(this.domainName);
        final com.amazonaws.services.cloudformation.model.Parameter projectNameParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("projectName").withParameterValue(this.projectName);
        final CreateStackRequest createStackRequest = new CreateStackRequest().withCapabilities(Capability.CAPABILITY_IAM).withStackName(projectName).withParameters(domainNameParameter, projectNameParameter);
        if (templateUrl == null) {
            String templateBody = new VpcInfraTemplateBody().decode();
            createStackRequest.withTemplateBody(templateBody);
        } else {
            createStackRequest.withTemplateURL(templateUrl);
        }
        final CreateStackResult stack = amazonCloudFormationClient.createStack(createStackRequest);
        DescribeStacksResult describeStacksResult = amazonCloudFormationClient.describeStacks(new DescribeStacksRequest().withStackName(projectName));
        while (describeStacksResult.getStacks().get(0).getStackStatus().equals(StackStatus.CREATE_IN_PROGRESS.toString())) {
            try {
                getLog().info("Awaiting stack create completion!");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                getLog().error(e);
                throw new RuntimeException(e);
            }
            describeStacksResult = amazonCloudFormationClient.describeStacks(new DescribeStacksRequest().withStackName(projectName));
        }
        final String stackStatus = describeStacksResult.getStacks().get(0).getStackStatus();
        if (!stackStatus.equals(StackStatus.CREATE_COMPLETE.toString())) {
            throw new MojoFailureException("Could not create infrastructure. Stack Status is: " + stackStatus + ". Please review details on the AWS console and open a new github issue on https://github.com/sdole/tvarit-maven/issues/new that is needed.");
        }
        getLog().info("Finished completing stack");

    }
}
