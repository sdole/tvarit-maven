/*
 * Tvarit is an AWS DevOps Automation Tool for JEE applications.
 * See http://www.tvarit.io
 *     Copyright (C) 2016. Sachin Dole.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.tvarit.plugin;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.*;
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
import org.apache.maven.project.MavenProject;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

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
    private String bucketName;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().debug("Starting " + this.getClass().getSimpleName() + " execution ");
        final BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        AmazonS3Client amazonS3Client = new AmazonS3Client(awsCredentials);
        amazonS3Client.putObject(new PutObjectRequest(bucketName, "lambda/deployNewWar.zip", this.getClass().getResourceAsStream("/lambda/deployNewWar.zip"), new ObjectMetadata()));
        final DescribeStacksRequest describeStacksRequest = new DescribeStacksRequest();
        describeStacksRequest.withStackName(projectName + "-infra");
        AmazonCloudFormationClient amazonCloudFormationClient = new AmazonCloudFormationClient(awsCredentials);
        final DescribeStacksResult describeStacksResult = amazonCloudFormationClient.describeStacks(describeStacksRequest);
        final String tvaritRoleOutput = describeStacksResult.getStacks().get(0).getOutputs().stream().filter(output -> "TvaritRole".equals(output.getOutputKey())).collect(Collectors.toList()).get(0).getOutputValue();
        final String tvaritInstanceProfileOutput = describeStacksResult.getStacks().get(0).getOutputs().stream().filter(output -> "TvaritInstanceProfile".equals(output.getOutputKey())).collect(Collectors.toList()).get(0).getOutputValue();


        AmazonEC2Client amazonEC2Client = new AmazonEC2Client(awsCredentials);
        final DescribeSubnetsRequest describeAppSubnetsRequest = new DescribeSubnetsRequest();
        final Filter subnetFilter = new Filter().withName("tag-key").withValues(projectName + ":appSubnet");
        describeAppSubnetsRequest.withFilters(subnetFilter);
        final DescribeVpcsRequest describeVpcsRequest = new DescribeVpcsRequest();
        describeVpcsRequest.withFilters(new Filter("tag-key", Collections.singletonList(projectName + ":vpc")));
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

        final com.amazonaws.services.cloudformation.model.Parameter projectNameParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("projectName").withParameterValue(this.projectName);
        final com.amazonaws.services.cloudformation.model.Parameter publicSubnetsParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("publicSubnets").withParameterValue(publicSubnets);
        final com.amazonaws.services.cloudformation.model.Parameter availabilityZonesParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("availabilityZones").withParameterValue(publicSubnetAzs);
        final com.amazonaws.services.cloudformation.model.Parameter vpcParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("vpc").withParameterValue(describeVpcResult.getVpcs().get(0).getVpcId());
        final com.amazonaws.services.cloudformation.model.Parameter healthCheckAbsoluteUrlParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("healthCheckAbsoluteUrl").withParameterValue("/tvarit/healthCheck.html");
        final com.amazonaws.services.cloudformation.model.Parameter tvaritRoleParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("tvaritRoleArn").withParameterValue(tvaritRoleOutput);
        final com.amazonaws.services.cloudformation.model.Parameter tvaritInstanceProfileParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("tvaritInstanceProfile").withParameterValue(tvaritInstanceProfileOutput);
        final com.amazonaws.services.cloudformation.model.Parameter tvaritBucketNameParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("bucketName").withParameterValue(this.bucketName);
        final String stackName = projectName + "-asg";
        final MavenProject project = (MavenProject) this.getPluginContext().getOrDefault("project", null);
        if (templateUrl == null)
            try {
                templateUrl = new TemplateUrlMaker().makeUrl(project, "autoscaling.template").toString();
            } catch (MalformedURLException e) {
                throw new MojoExecutionException("Could not create default url for templates. Please open an issue on github.", e);
            }
        final CreateStackRequest createStackRequest =
                new CreateStackRequest().
                        withCapabilities(Capability.CAPABILITY_IAM).
                        withStackName(stackName).
                        withParameters(
                                projectNameParameter,
                                availabilityZonesParameter,
                                publicSubnetsParameter,
                                vpcParameter,
                                healthCheckAbsoluteUrlParameter,
                                tvaritInstanceProfileParameter,
                                tvaritRoleParameter,
                                tvaritBucketNameParameter
                        ).
                        withTemplateURL(templateUrl);
        final Stack stack = new StackMaker().makeStack(createStackRequest, amazonCloudFormationClient, getLog());
        final List<Output> outputs = stack.getOutputs();
        final String lambdaFunctionArn = outputs.stream().filter(output -> output.getOutputKey().equals("LambdaFunctionArn")).findFirst().get().getOutputValue();

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
