package com.tvarit.plugin;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.opsworks.AWSOpsWorksClient;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "hello", defaultPhase = LifecyclePhase.COMPILE)
public class TvaritMojo extends AbstractMojo {

    @Parameter(required = false, alias = "stack")
    private String stackName;

    @Parameter(required = false, alias = "layer")
    private String layerName;
    @Parameter(required = true, alias = "role_arn")
    private String roleArn;
    @Parameter(required = true, alias = "accessKey")
    private String accessKey;
    @Parameter(required = true, alias = "secretKey", readonly = true)
    private String secretKey;
    @Parameter(required = true, alias = "instance_profile_arn")
    private String instanceProfileArn;

//    @Inject
    private StackCreator stackCreator=new StackCreator();
//    @Inject
    private LayerCreator layerCreator=new LayerCreator();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().debug("Starting " + this.getClass().getSimpleName() + " execution ");
        AWSOpsWorksClient awsOpsWorksClient = new AWSOpsWorksClient(new BasicAWSCredentials(accessKey, secretKey));
        final MavenProject project = (MavenProject) this.getPluginContext().getOrDefault("project", null);
        if (stackName == null) {
            stackName = project.getGroupId() + "-" + project.getArtifactId() + "-" + "stack";
        }

        if (layerName == null) {
            layerName = project.getGroupId() + "-" + project.getArtifactId() + "-" + "layer";
        }

        final String stack = stackCreator.create(awsOpsWorksClient, stackName, this, roleArn, layerName, instanceProfileArn);
        final String layerId = layerCreator.create(awsOpsWorksClient, layerName, this, stack);
        getLog().debug("Done!");
    }

}