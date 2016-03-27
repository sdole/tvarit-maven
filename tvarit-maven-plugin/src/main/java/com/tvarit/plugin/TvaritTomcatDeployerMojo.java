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
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.model.AttachInstancesRequest;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.net.MalformedURLException;

@Mojo(name = "deploy-tomcat-ui-app")
public class TvaritTomcatDeployerMojo extends AbstractMojo {
    @Parameter(required = true, readonly = true, property = "myAccessKey")
    private String accessKey;
    @Parameter(required = true, readonly = true, property = "mySecretKey")
    private String secretKey;
    @Parameter(required = true)
    private String bucketName;
    @Parameter(required = true)
    private String projectName;
    @Parameter
    private String templateUrl;
    @Parameter(required = true)
    private String commaSeparatedSubnetIds;
    @Parameter(required = true)
    private String tvaritRole;
    @Parameter(required = true)
    private String tvaritInstanceProfile;
    @Parameter(required = true)
    private String instanceSecurityGroupId;
    @Parameter(required = true)
    private String sshKeyName;
    @Parameter(required = true)
    private String autoScalingGroupName;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        AmazonS3Client s3Client = new AmazonS3Client(awsCredentials);
        final MavenProject project = (MavenProject) this.getPluginContext().getOrDefault("project", null);
        final File warFile = project.getArtifact().getFile();
        final String key = "deployables/" + project.getGroupId() + "/" + project.getArtifactId() + "/" + project.getVersion() + "/" + warFile.getName();
        final PutObjectResult putObjectResult = s3Client.putObject(new PutObjectRequest(bucketName, key, warFile));
        AmazonCloudFormationClient amazonCloudFormationClient = new AmazonCloudFormationClient(awsCredentials);
        final com.amazonaws.services.cloudformation.model.Parameter projectNameParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("projectName").withParameterValue(this.projectName);
        final com.amazonaws.services.cloudformation.model.Parameter publicSubnetsParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("publicSubnets").withParameterValue(commaSeparatedSubnetIds);
        final com.amazonaws.services.cloudformation.model.Parameter tvaritRoleParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("tvaritRole").withParameterValue(tvaritRole);
        final com.amazonaws.services.cloudformation.model.Parameter tvaritInstanceProfileParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("tvaritInstanceProfile").withParameterValue(this.tvaritInstanceProfile);
        final com.amazonaws.services.cloudformation.model.Parameter tvaritBucketNameParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("bucketName").withParameterValue(this.bucketName);
        final com.amazonaws.services.cloudformation.model.Parameter instanceSecurityGroupIdParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("sgId").withParameterValue(this.instanceSecurityGroupId);
        final com.amazonaws.services.cloudformation.model.Parameter sshKeyNameParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("keyName").withParameterValue(this.sshKeyName);
        final String warFileUrl = s3Client.getUrl(bucketName, key).toString();
        final com.amazonaws.services.cloudformation.model.Parameter warFileUrlParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("warFileUrl").withParameterValue(warFileUrl);
        final CreateStackRequest createStackRequest = new CreateStackRequest();
        if (templateUrl == null) {
            try {
                templateUrl = new TemplateUrlMaker().makeUrl(project, "newinstance.template").toString();
            } catch (MalformedURLException e) {
                throw new MojoExecutionException("Could not create default url for templates. Please open an issue on github.", e);
            }
        }
        createStackRequest.
                withStackName(projectName + "-instance-" + project.getVersion().replace(".", "-")).
                withParameters(
                        projectNameParameter,
                        publicSubnetsParameter,
                        tvaritInstanceProfileParameter,
                        tvaritRoleParameter,
                        tvaritBucketNameParameter,
                        instanceSecurityGroupIdParameter,
                        warFileUrlParameter,
                        sshKeyNameParameter
                ).
                withDisableRollback(true).
                withTemplateURL(templateUrl);
        createStackRequest.withDisableRollback(true);
        final Stack stack = new StackMaker().makeStack(createStackRequest, amazonCloudFormationClient, getLog());
        AmazonAutoScalingClient amazonAutoScalingClient = new AmazonAutoScalingClient(awsCredentials);
        final AttachInstancesRequest attachInstancesRequest = new AttachInstancesRequest();
        attachInstancesRequest.withInstanceIds(stack.getOutputs().get(0).getOutputValue(), stack.getOutputs().get(1).getOutputValue()).withAutoScalingGroupName(autoScalingGroupName);
        amazonAutoScalingClient.attachInstances(attachInstancesRequest);
    }
}
