package com.tvarit.plugin;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.Capability;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.Collections;

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
        final String publicSubnetAzs = publicSubnetIdBuilder.deleteCharAt(publicSubnetAzsBuilder.length() - 1).toString();
        AmazonCloudFormationClient amazonCloudFormationClient = new AmazonCloudFormationClient(awsCredentials);
        final com.amazonaws.services.cloudformation.model.Parameter projectNameParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("projectName").withParameterValue(this.projectName);
        final com.amazonaws.services.cloudformation.model.Parameter publicSubnetsParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("publicSubnets").withParameterValue(publicSubnets);
        final com.amazonaws.services.cloudformation.model.Parameter availabilityZonesParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("availabilityZones").withParameterValue(publicSubnetAzs);
        final com.amazonaws.services.cloudformation.model.Parameter vpcParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("vpc").withParameterValue(describeVpcResult.getVpcs().get(0).getVpcId());
        final CreateStackRequest createStackRequest = new CreateStackRequest().withCapabilities(Capability.CAPABILITY_IAM).withStackName(projectName).withParameters(projectNameParameter, availabilityZonesParameter, publicSubnetsParameter);
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
