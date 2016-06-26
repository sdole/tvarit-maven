package com.tvarit.plugin.vpc;

import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.Capability;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.tvarit.plugin.env.TvaritEnvironment;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class MakeVpcDelegate {
    public void make() {
        //TODO add the router with minxize=mazsize=desired = 0
        TvaritEnvironment tvaritEnvironment = TvaritEnvironment.getInstance();
        AmazonCloudFormationClient cloudformationClient = TvaritEnvironment.getInstance().getAmazonCloudFormationClient();
        CreateStackRequest createVpcStackRequest = new CreateStackRequest();
        URL url;
        try {
            url = tvaritEnvironment.getTemplateUrlMaker().makeUrl("base/main.template");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        String versionSuffix = TvaritEnvironment.getInstance().getMavenProject().getVersion().replace(".", "-");
        String projectName = tvaritEnvironment.getProjectName();
        createVpcStackRequest.withTemplateURL(url.toString()).withStackName(projectName + "-" + versionSuffix);
        List<Parameter> makeVpcParameters = new MakeVpcParameterMaker().make();
        createVpcStackRequest.withParameters(makeVpcParameters).withCapabilities(Capability.CAPABILITY_IAM);
        cloudformationClient.createStack(createVpcStackRequest);
    }
}
