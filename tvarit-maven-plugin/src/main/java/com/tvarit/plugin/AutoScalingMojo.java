package com.tvarit.plugin;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.Capability;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.Output;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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
    private String tvaritInstanceProfile;
    @Parameter(required = true)
    private String bucketName;
    @Parameter(required = true)
    private String tvaritRoleArn;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().debug("Starting " + this.getClass().getSimpleName() + " execution ");
        final BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        AmazonS3Client amazonS3Client = new AmazonS3Client(awsCredentials);
        amazonS3Client.putObject(new PutObjectRequest(bucketName, "lambda/deployNewWar.zip", this.getClass().getResourceAsStream("/lambda/deployNewWar.zip"), new ObjectMetadata()));


        AmazonEC2Client amazonEC2Client = new AmazonEC2Client(awsCredentials);
        final DescribeSubnetsRequest describeAppSubnetsRequest = new DescribeSubnetsRequest();
        final Filter filter = new Filter().withName("tag-key").withValues("tvarit:app");
        describeAppSubnetsRequest.withFilters(filter);
        final DescribeVpcsRequest describeVpcsRequest = new DescribeVpcsRequest();
        describeVpcsRequest.withFilters(new Filter("tag-key", Collections.singletonList("tvarit:vpc")));
        final DescribeVpcsResult describeVpcResult = amazonEC2Client.describeVpcs(describeVpcsRequest);
        final DescribeSubnetsResult describeSubnetsResult = amazonEC2Client.describeSubnets(describeAppSubnetsRequest);
        StringBuilder publicSubnetAzsBuilder = new StringBuilder();
        StringBuilder publicSubnetIdBuilder = new StringBuilder();
        describeSubnetsResult.getSubnets().stream().forEach(eachSubnet -> {
            publicSubnetAzsBuilder.append(eachSubnet.getAvailabilityZone()).append(",");
            publicSubnetIdBuilder.append(eachSubnet.getSubnetId()).append(",");
        });
        final String publicSubnets = publicSubnetIdBuilder.deleteCharAt(publicSubnetIdBuilder.length() - 1).toString();
        final String publicSubnetAzs = publicSubnetAzsBuilder.deleteCharAt(publicSubnetAzsBuilder.length() - 1).toString();
        AmazonCloudFormationClient amazonCloudFormationClient = new AmazonCloudFormationClient(awsCredentials);
        final com.amazonaws.services.cloudformation.model.Parameter projectNameParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("projectName").withParameterValue(this.projectName);
        final com.amazonaws.services.cloudformation.model.Parameter publicSubnetsParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("publicSubnets").withParameterValue(publicSubnets);
        final com.amazonaws.services.cloudformation.model.Parameter availabilityZonesParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("availabilityZones").withParameterValue(publicSubnetAzs);
        final com.amazonaws.services.cloudformation.model.Parameter vpcParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("vpc").withParameterValue(describeVpcResult.getVpcs().get(0).getVpcId());
        final com.amazonaws.services.cloudformation.model.Parameter healthCheckAbsoluteUrlParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("healthCheckAbsoluteUrl").withParameterValue("/healthCheck.html");
        final com.amazonaws.services.cloudformation.model.Parameter tvaritRoleParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("tvaritRoleArn").withParameterValue(tvaritRoleArn);
        final com.amazonaws.services.cloudformation.model.Parameter tvaritInstanceProfileParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("tvaritInstanceProfile").withParameterValue(this.tvaritInstanceProfile);
        final com.amazonaws.services.cloudformation.model.Parameter tvaritBucketNameParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("bucketName").withParameterValue(this.bucketName);
        final String stackName = projectName + "-asg";
        final CreateStackRequest createStackRequest = new CreateStackRequest().withCapabilities(Capability.CAPABILITY_IAM).withStackName(stackName).withParameters(projectNameParameter, availabilityZonesParameter, publicSubnetsParameter, vpcParameter, healthCheckAbsoluteUrlParameter, tvaritInstanceProfileParameter, tvaritRoleParameter, tvaritBucketNameParameter);
        if (templateUrl == null) {
            String templateBody = new TemplateReader().readTemplate("/autoscaling.template");
            createStackRequest.withTemplateBody(templateBody);
        } else {
            createStackRequest.withTemplateURL(templateUrl);
        }
        final Stack stack = new StackMaker().makeStack(createStackRequest, amazonCloudFormationClient, getLog());
        final List<Output> outputs = stack.getOutputs();
        final String lambdaFunctionArn = outputs.stream().filter(output -> output.getOutputKey().equals("LambdaFunctionArn")).findFirst().get().getOutputValue();

        final ObjectMetadata autoscalingNewInstanceTemplateS3ObjMetadata = new ObjectMetadata();
        amazonS3Client.putObject(new PutObjectRequest(bucketName, "config/autoscaling/newinstance.template", this.getClass().getResourceAsStream("/newinstance.template"), autoscalingNewInstanceTemplateS3ObjMetadata));
        final BucketNotificationConfiguration notificationConfiguration = new BucketNotificationConfiguration();
        final HashMap<String, NotificationConfiguration> configurations = new HashMap<>();
        final LambdaConfiguration lambdaConfiguration = new LambdaConfiguration(lambdaFunctionArn);
        final HashSet<String> events = new HashSet<>();
        events.add("s3:ObjectCreated:*");
        lambdaConfiguration.setEvents(events);
        final com.amazonaws.services.s3.model.Filter notificationFilter = new com.amazonaws.services.s3.model.Filter();
        final S3KeyFilter s3KeyFilter = new S3KeyFilter();
        notificationFilter.withS3KeyFilter(s3KeyFilter);
        s3KeyFilter.withFilterRules(new FilterRule().withName("prefix").withValue("config/autoscale.json"));
        lambdaConfiguration.setFilter(notificationFilter);
        configurations.put("warUploaded", lambdaConfiguration);
        notificationConfiguration.setConfigurations(configurations);
        final SetBucketNotificationConfigurationRequest setBucketNotificationConfigurationRequest = new SetBucketNotificationConfigurationRequest(bucketName, notificationConfiguration);
        amazonS3Client.setBucketNotificationConfiguration(setBucketNotificationConfigurationRequest);
        getLog().info("Finished completing stack");

    }
}
