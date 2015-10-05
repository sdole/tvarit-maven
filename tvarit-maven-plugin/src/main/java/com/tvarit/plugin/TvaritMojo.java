package com.tvarit.plugin;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
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

    @Parameter(required = false, alias = "subnet")
    private String subnetName;
    @Parameter(required = false, alias = "vpc")
    private String vpcName;
    @Parameter(required = false, alias = "stack")
    private String stackName;
    @Parameter(required = false, alias = "layer")
    private String layerName;
    @Parameter(required = true, alias = "role_arn")
    private String roleArn;
    @Parameter(required = true, alias = "accessKey",readonly = true)
    private String accessKey;
    @Parameter(required = true, alias = "secretKey", readonly = true)
    private String secretKey;
    @Parameter(required = true,alias ="baseName")
    private String baseName;

    @Parameter(required = true, alias = "instance_profile_arn")
    private String instanceProfileArn;

    private InfrastructureCreator infrastructureCreator = new InfrastructureCreator();
    private InstanceCreator instanceCreator = new InstanceCreator();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().debug("Starting " + this.getClass().getSimpleName() + " execution ");
        final BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        AWSOpsWorksClient awsOpsWorksClient = new AWSOpsWorksClient(awsCredentials);
        final AmazonEC2Client amazonEc2Client = new AmazonEC2Client(awsCredentials);
        final MavenProject project = (MavenProject) this.getPluginContext().getOrDefault("project", null);
        if (vpcName == null) {
            vpcName = baseName + "-" + "vpc";
        }
        if (subnetName == null) {
            subnetName = baseName + "-" + "subnet";
        }

        if (stackName == null) {
            stackName = baseName + "-" + "stack";
        }

        if (layerName == null) {
            layerName = baseName + "-" + "layer";
        }

        final InfrastructureIds infrastructureIds = infrastructureCreator.create(this, awsOpsWorksClient, amazonEc2Client, vpcName, subnetName, stackName, layerName, roleArn, instanceProfileArn);

        final String instanceId = instanceCreator.create(this, awsOpsWorksClient, infrastructureIds);
        InstanceStarter instanceStarter = new InstanceStarter();
        instanceStarter.start(awsOpsWorksClient, instanceId);
        getLog().debug("Done!");
    }

}
