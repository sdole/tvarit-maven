package com.tvarit.plugin;

import com.amazonaws.auth.BasicAWSCredentials;
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

@Mojo(name = "deploy-tomcat-ui-app")
public class TvaritTomcatDeployerMojo extends AbstractMojo {
    @Parameter(required = true, readonly = true, property = "myAccessKey")
    private String accessKey;
    @Parameter(required = true, readonly = true, property = "mySecretKey")
    private String secretKey;
    @Parameter(required = true)
    private String bucketName;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        AmazonS3Client s3Client = new AmazonS3Client(new BasicAWSCredentials(accessKey, secretKey));
        final MavenProject project = (MavenProject) this.getPluginContext().getOrDefault("project", null);
        final File warFile = project.getArtifact().getFile();
        final String key = "war/" + project.getGroupId() + "/" + project.getArtifactId() + "/" + project.getVersion() + "/" + warFile.getName();
        final PutObjectResult putObjectResult = s3Client.putObject(new PutObjectRequest(bucketName, key, warFile));
    }
}
