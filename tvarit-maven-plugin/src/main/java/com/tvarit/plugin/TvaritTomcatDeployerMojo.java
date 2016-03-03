package com.tvarit.plugin;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.StringInputStream;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

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
        final String activeJson = "{active:\"" + key + "\"}";
        final ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("application/json");
        metadata.setContentEncoding("UTF-8");
        final int length = activeJson.length();
        metadata.setContentLength(length);
        final StringInputStream input;
        try {
            input = new StringInputStream(activeJson);
        } catch (UnsupportedEncodingException e) {
            throw new MojoExecutionException("Could not create string input stream.", e);
        }
        s3Client.putObject(new PutObjectRequest(bucketName, "config/active.json", input, metadata));
        final S3Object infrastructureS3Object = s3Client.getObject(new GetObjectRequest(bucketName, "config/infrastructure.json"));
        final int contentLength = (int) infrastructureS3Object.getObjectMetadata().getContentLength();
        final byte[] b = new byte[contentLength];
        try {
            infrastructureS3Object.getObjectContent().read(b, 0, contentLength);
        } catch (IOException e) {
            throw new MojoFailureException("Could not read infrastructure configuration.", e);
        }
        final JsonNode jsonNode;
        try {
            jsonNode = new ObjectMapper().readTree(b);
        } catch (IOException e) {
            throw new MojoFailureException("Could not read infrastructure configuration.", e);
        }

    }
}
