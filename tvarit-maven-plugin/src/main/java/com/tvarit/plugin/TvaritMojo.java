package com.tvarit.plugin;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.opsworks.AWSOpsWorksClient;
import com.amazonaws.services.opsworks.model.DescribeStacksRequest;
import com.amazonaws.services.opsworks.model.DescribeStacksResult;
import com.amazonaws.services.opsworks.model.Stack;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.stream.Collectors;

@Mojo(name = "hello", defaultPhase = LifecyclePhase.COMPILE)
public class TvaritMojo extends AbstractMojo {

    @Parameter(alias = "stack", required = true)
    private String stackName;

    @Parameter(required = true, property = "accessKey")
    private String accessKey;
    @Parameter(required = true, property = "secretKey")
    private String secretKey;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().debug("Starting " + this.getClass().getSimpleName() + " execution ");
        getLog().debug("stackName:-" + stackName + "-");
        AWSOpsWorksClient awsOpsWorksClient = new AWSOpsWorksClient(new BasicAWSCredentials(accessKey, secretKey));
        DescribeStacksRequest describeStacksRequest = new DescribeStacksRequest();
//        describeStacksRequest.withStackIds(stackName);
        DescribeStacksResult describeStacksResult = awsOpsWorksClient.describeStacks(describeStacksRequest);

        getLog().debug(describeStacksResult.getStacks().stream().map(Stack::getName).collect(Collectors.toList()).toString());
    }

}
