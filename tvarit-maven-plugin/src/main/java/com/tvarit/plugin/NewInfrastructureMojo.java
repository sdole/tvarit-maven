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
import com.amazonaws.services.cloudformation.model.Capability;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.InputStream;

@Mojo(name = "make-infrastructure")
public class NewInfrastructureMojo extends AbstractMojo {
    @Parameter(required = true, readonly = true, property = "mySecretKey")
    private String secretKey;
    @Parameter(required = true, readonly = true, property = "myAccessKey")
    private String accessKey;
    @Parameter
    private String templateBodyFile;
    @Parameter(required = true)
    private String projectName;
    @Parameter(required = true)
    private String domainName;
    @Parameter(required = true)
    private String bucketName;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().debug("Starting " + this.getClass().getSimpleName() + " execution ");
        final BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        AmazonCloudFormationClient amazonCloudFormationClient = new AmazonCloudFormationClient(awsCredentials);
        final com.amazonaws.services.cloudformation.model.Parameter domainNameParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("domainName").withParameterValue(this.domainName);
        final com.amazonaws.services.cloudformation.model.Parameter projectNameParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("projectName").withParameterValue(this.projectName);
        this.bucketName = "tvarit-" + this.bucketName;
        final com.amazonaws.services.cloudformation.model.Parameter bucketNameParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("bucketName").withParameterValue(this.bucketName);
        final String template;

        if (templateBodyFile == null) templateBodyFile = "/vpc-infra.template";
        else if (!templateBodyFile.startsWith("/")) templateBodyFile = "/" + templateBodyFile;

        template = new TemplateReader().readTemplate(templateBodyFile);
        final CreateStackRequest createStackRequest =
                new CreateStackRequest().
                        withCapabilities(Capability.CAPABILITY_IAM).
                        withStackName(projectName + "-infra").
                        withParameters(domainNameParameter, projectNameParameter, bucketNameParameter).
                        withTemplateBody(template);
        new StackMaker().makeStack(createStackRequest, amazonCloudFormationClient, getLog());
        AmazonS3Client s3Client = new AmazonS3Client(awsCredentials);
        final MavenProject project = (MavenProject) this.getPluginContext().getOrDefault("project", null);
        this.uploadInfrastructureTemplate(s3Client, project, "/infrastructure.template", templateBodyFile);
        this.uploadInfrastructureTemplate(s3Client, project, "/autoscaling.template", "/autoscaling.template");
        this.uploadInfrastructureTemplate(s3Client, project, "/newinstance.template", "/newinstance.template");
        getLog().info("Finished completing stack");

    }


    private void uploadInfrastructureTemplate(AmazonS3Client s3Client, MavenProject project, String fileName, String templateBodyFileName) {
        final ObjectMetadata infraTemplateMetadata = new ObjectMetadata();
        infraTemplateMetadata.setContentType("application/json");
        final String infraTemplateKeyName = "cfn-templates/" + project.getGroupId() + "/" + project.getArtifactId() + "/" + project.getVersion() + fileName;
        final InputStream infraTemplateInputStream = this.getClass().getResourceAsStream(templateBodyFileName);
        final PutObjectRequest putObjectRequest = new PutObjectRequest(this.bucketName, infraTemplateKeyName, infraTemplateInputStream, infraTemplateMetadata);
        s3Client.putObject(putObjectRequest);

    }
}
