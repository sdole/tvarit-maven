package com.tvarit.plugin;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.Capability;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
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

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().debug("Starting " + this.getClass().getSimpleName() + " execution ");
        final BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        AmazonCloudFormationClient amazonCloudFormationClient = new AmazonCloudFormationClient(awsCredentials);
        final com.amazonaws.services.cloudformation.model.Parameter projectNameParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("projectName").withParameterValue(this.projectName);
        final CreateStackRequest createStackRequest = new CreateStackRequest().withCapabilities(Capability.CAPABILITY_IAM).withStackName(projectName).withParameters(projectNameParameter);
        if (templateUrl == null) {
            String templateBody = new VpcInfraTemplateBody().decode();
            createStackRequest.withTemplateBody(templateBody);
        } else {
            createStackRequest.withTemplateURL(templateUrl);
        }
        final String stackStatus = new StackMaker().makeStack(createStackRequest, amazonCloudFormationClient, getLog());
        getLog().info("Finished completing stack");

    }
}
