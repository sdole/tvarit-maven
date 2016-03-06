package com.tvarit.plugin;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.Capability;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Mojo(name = "make-infra")
public class InfrastructureMojo extends AbstractMojo {
    @Parameter(required = true, readonly = true, property = "mySecretKey")
    private String secretKey;
    @Parameter(required = true, readonly = true, property = "myAccessKey")
    private String accessKey;
    @Parameter
    private String templateUrl;
    @Parameter(required = true)
    private String projectName;
    @Parameter(required = true)
    private String domainName;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().debug("Starting " + this.getClass().getSimpleName() + " execution ");
        final BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        AmazonCloudFormationClient amazonCloudFormationClient = new AmazonCloudFormationClient(awsCredentials);
        final com.amazonaws.services.cloudformation.model.Parameter domainNameParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("domainName").withParameterValue(this.domainName);
        final com.amazonaws.services.cloudformation.model.Parameter projectNameParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("projectName").withParameterValue(this.projectName);
        final CreateStackRequest createStackRequest = new CreateStackRequest().withCapabilities(Capability.CAPABILITY_IAM).withStackName(projectName).withParameters(domainNameParameter, projectNameParameter);
        if (templateUrl == null) {
            final BufferedReader templateStream = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/vpc-infra.template")));
            StringBuilder sb = new StringBuilder();
            try {
                String line;
                while ((line = templateStream.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                throw new MojoFailureException("Could not read vpc infra template.", e);
            }
            createStackRequest.withTemplateBody(sb.toString());
        } else {
            createStackRequest.withTemplateURL(templateUrl);
        }
        new StackMaker().makeStack(createStackRequest, amazonCloudFormationClient, getLog());
        getLog().info("Finished completing stack");

    }
}
