package com.tvarit.plugin.base;

import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.Capability;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.tvarit.plugin.env.TvaritEnvironment;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class MakeBaseInfrastructureDelegate {
    public void make() {
        TvaritEnvironment tvaritEnvironment = TvaritEnvironment.getInstance();
        AmazonCloudFormationClient amazonCloudFormationClient = TvaritEnvironment.getInstance().getAmazonCloudFormationClient();
        CreateStackRequest createVpcStackRequest = new CreateStackRequest();
        URL url;
        try {
            url = tvaritEnvironment.getTemplateUrlMaker().makeUrl("base/main.template");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        createVpcStackRequest.withTemplateURL(url.toString()).withStackName("tvarit-base-infrastructure3");
        List<Parameter> makeVpcParameters = new MakeBaseInfrastructureParameterMaker().make();
        createVpcStackRequest.withParameters(makeVpcParameters).withCapabilities(Capability.CAPABILITY_IAM);
        amazonCloudFormationClient.createStack(createVpcStackRequest);
    }
}
