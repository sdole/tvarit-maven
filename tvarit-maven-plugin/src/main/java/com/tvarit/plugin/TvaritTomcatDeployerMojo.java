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
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
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
import java.util.HashMap;

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
    private String sshKeyName;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        AmazonEC2Client ec2Client = new AmazonEC2Client(awsCredentials);
        AmazonAutoScalingClient autoScalingClient = new AmazonAutoScalingClient(awsCredentials);
        final MavenProject project = (MavenProject) this.getPluginContext().getOrDefault("project", null);
        String projectGroupId = project.getGroupId();
        String projectArtifactId = project.getArtifactId();
        String projectVersion = project.getVersion();
        new IsThisAnUpdate(autoScalingClient).find(projectGroupId, projectArtifactId, projectVersion).perform();

        if (templateUrl == null)
            try {
                templateUrl = new TemplateUrlMaker().makeUrl(project, "newinstance.template").toString();
            } catch (MalformedURLException e) {
                throw new MojoExecutionException("Could not create default url for templates. Please open an issue on github.", e);
            }

        AmazonS3Client s3Client = new AmazonS3Client(awsCredentials);
        final File warFile = project.getArtifact().getFile();
        final String key = "deployables/" + projectGroupId + "/" + projectArtifactId + "/" + projectVersion + "/" + warFile.getName();
        final PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, warFile);
        final ObjectMetadata metadata = new ObjectMetadata();
        final HashMap<String, String> userMetadata = new HashMap<>();
        userMetadata.put("project_name", projectName);
        userMetadata.put("stack_template_url", templateUrl);
        userMetadata.put("private_key_name", sshKeyName);
        metadata.setUserMetadata(userMetadata);
        putObjectRequest.withMetadata(metadata);
        final PutObjectResult putObjectResult = s3Client.putObject(putObjectRequest);

    }
}
