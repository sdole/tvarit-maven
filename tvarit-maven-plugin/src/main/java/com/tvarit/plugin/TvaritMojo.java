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

@Mojo(name = "hello", defaultPhase = LifecyclePhase.DEPLOY)
public class TvaritMojo extends AbstractMojo {

    @Parameter(required = true)
    private String roleArn;
    @Parameter(required = true, readonly = true,property = "myAccessKey")
    private String accessKey;
    @Parameter(required = true,  readonly = true,property = "mySecretKey")
    private String secretKey;
    @Parameter(required = true)
    private String baseName;

    @Parameter(required = true)
    private String instanceProfileArn;

    private InfrastructureCreator infrastructureCreator = new InfrastructureCreator();
    private InstanceCreator instanceCreator = new InstanceCreator();
    private InstanceStarter instanceStarter = new InstanceStarter();
    private WarAppDeployer warAppDeployer = new WarAppDeployer();
    private WaitTillInstanceOnline waitTillInstanceOnline = new  WaitTillInstanceOnline();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().debug("Starting " + this.getClass().getSimpleName() + " execution ");
        final BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        AWSOpsWorksClient awsOpsWorksClient = new AWSOpsWorksClient(awsCredentials);
        final AmazonEC2Client amazonEc2Client = new AmazonEC2Client(awsCredentials);
        final MavenProject project = (MavenProject) this.getPluginContext().getOrDefault("project", null);
        final InfrastructureIds infrastructureIds = infrastructureCreator.create(this, awsOpsWorksClient, amazonEc2Client, roleArn, instanceProfileArn, baseName);
        final String instanceId = instanceCreator.create(this, awsOpsWorksClient, infrastructureIds);
        instanceStarter.start(awsOpsWorksClient, instanceId);
        waitTillInstanceOnline.waitTillInstanceOnline(awsOpsWorksClient,instanceId);
        warAppDeployer.deploy(project,getLog(),awsOpsWorksClient,instanceId);
        getLog().debug("Done!");
    }

}
