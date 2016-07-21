package com.tvarit.plugin.env;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.s3.AmazonS3Client;
import com.tvarit.plugin.AbstractTvaritMojo;
import com.tvarit.plugin.TemplateUrlMaker;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

public final class TvaritEnvironment {
    private MavenProject mavenProject;
    private Log logger;
    private TemplateUrlMaker templateUrlMaker;
    private static TvaritEnvironment instance;
    private AmazonCloudFormationClient amazonCloudFormationClient;
    private AbstractTvaritMojo mojo;
    private AmazonS3Client amazonS3Client;

    public MavenProject getMavenProject() {
        return mavenProject;
    }

    public Log getLogger() {
        return logger;
    }

    public TemplateUrlMaker getTemplateUrlMaker() {
        return templateUrlMaker;
    }


    public static synchronized TvaritEnvironment init(AbstractTvaritMojo mojo) {
        if (instance == null) {
            instance = new TvaritEnvironment();
            instance.mavenProject = (MavenProject) mojo.getPluginContext().getOrDefault("project", null);
            instance.logger = mojo.getLog();
            String awsAuthProfile = mojo.getAwsAuthProfile();
            AWSCredentialsProvider awsCredentialsProvider;
            if (awsAuthProfile != null) {
                awsCredentialsProvider = new ProfileCredentialsProvider(awsAuthProfile);
            } else {
                awsCredentialsProvider = new DefaultAWSCredentialsProviderChain();
            }
            instance.amazonCloudFormationClient = new AmazonCloudFormationClient(awsCredentialsProvider);
            instance.amazonS3Client = new AmazonS3Client(awsCredentialsProvider);
            instance.templateUrlMaker = new TemplateUrlMaker();
            instance.mojo = mojo;
        } else {
            throw new RuntimeException("already initialized");
        }
        return instance;
    }

    public static TvaritEnvironment getInstance() {
        return instance;
    }

    public AmazonCloudFormationClient getAmazonCloudFormationClient() {
        return amazonCloudFormationClient;
    }

    public <T extends AbstractTvaritMojo> T getMojo() {
        return (T) mojo;
    }

    public String getProjectName() {
        return mavenProject.getGroupId() + "-" + mavenProject.getArtifactId();
    }

    public AmazonS3Client getAmazonS3Client() {
        return amazonS3Client;
    }

    public String getArtifactBucketName() {
        return getMojo().getArtifactBucketName() == null ? "tvarit-" + getProjectName() : getMojo().getArtifactBucketName();
    }


}
