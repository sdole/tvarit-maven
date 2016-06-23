package com.tvarit.plugin.env;

import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.lambda.model.Runtime;
import com.tvarit.plugin.TemplateUrlMaker;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

public final class TvaritEnvironment {
    private MavenProject mavenProject;
    private Log logger;
    private TemplateUrlMaker templateUrlMaker;
    private static TvaritEnvironment instance;
    private AmazonCloudFormationClient amazonCloudFormationClient;
    private String projectName;

    public MavenProject getMavenProject() {
        return mavenProject;
    }

    public Log getLogger() {
        return logger;
    }

    public TemplateUrlMaker getTemplateUrlMaker() {
        return templateUrlMaker;
    }


    public static TvaritEnvironment init(AbstractMojo mojo, String projectName) {
        if (instance == null) {
            instance = new TvaritEnvironment();
            instance.mavenProject = (MavenProject) mojo.getPluginContext().getOrDefault("project", null);
            instance.logger = mojo.getLog();
            instance.amazonCloudFormationClient = new AmazonCloudFormationClient();
            instance.templateUrlMaker = new TemplateUrlMaker();
            instance.projectName = projectName;
        } else {
            throw new RuntimeException("already initialized");
        }
        return instance;
    }

    public static TvaritEnvironment getInstance(){
        return instance;
    }

    public AmazonCloudFormationClient getAmazonCloudFormationClient() {
        return amazonCloudFormationClient;
    }

    public String getProjectName() {
        return projectName;
    }
}
