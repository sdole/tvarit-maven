package com.tvarit.plugin.vpc;

import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.Capability;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.tvarit.plugin.MakeVpcMojo;
import com.tvarit.plugin.env.TvaritEnvironment;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class MakeVpcDelegate {
    public void make() {
        TvaritEnvironment tvaritEnvironment = TvaritEnvironment.getInstance();
        AmazonCloudFormationClient cloudformationClient = new AmazonCloudFormationClient();
        CreateStackRequest createVpcStackRequest = new CreateStackRequest();
        URL url;
        try {
            url = tvaritEnvironment.getTemplateUrlMaker().makeUrl("router.template");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        String stackNameSuffix = TvaritEnvironment.getInstance().getMavenProject().getArtifactId() + "-" + TvaritEnvironment.getInstance().getMavenProject().getVersion().replace(".", "-");
        String projectName = tvaritEnvironment.<MakeVpcMojo>getMojo().getProjectName();
        createVpcStackRequest.withTemplateURL(url.toString()).withStackName("tvarit-" + projectName + "-" + stackNameSuffix);
        List<Parameter> makeVpcParameters = new MakeVpcParameterMaker().make();
        createVpcStackRequest.withParameters(makeVpcParameters).withCapabilities(Capability.CAPABILITY_IAM);
        cloudformationClient.createStack(createVpcStackRequest);
    }
}
