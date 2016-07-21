package com.tvarit.plugin.base;

import com.amazonaws.services.cloudformation.model.Parameter;
import com.tvarit.plugin.MakeBaseInfrastructureMojo;
import com.tvarit.plugin.TemplateUrlMaker;
import com.tvarit.plugin.env.TvaritEnvironment;
import edu.emory.mathcs.backport.java.util.Arrays;

import java.net.MalformedURLException;
import java.util.List;

class MakeBaseInfrastructureParameterMaker {
    List<Parameter> make() {
        String projectName = TvaritEnvironment.getInstance().getProjectName();
        String artifactBucketName = TvaritEnvironment.getInstance().<MakeBaseInfrastructureMojo>getMojo().getArtifactBucketName();
        String availabilityZones = TvaritEnvironment.getInstance().<MakeBaseInfrastructureMojo>getMojo().getAvailabilityZones();
        final Parameter bucketNameParm = new Parameter().withParameterKey("ArtifactBucketNameParm").withParameterValue(artifactBucketName);
        final Parameter projectNameParm = new Parameter().withParameterKey("ProjectNameParm").withParameterValue(projectName);
        final Parameter availabilityZonesParm = new Parameter().withParameterKey("AvailabilityZones").withParameterValue(availabilityZones);
        String routerTemplateUrl;
        String networkTemplateUrl;
        try {
            networkTemplateUrl = new TemplateUrlMaker().makeUrl("base/network.template").toString();
            routerTemplateUrl = new TemplateUrlMaker().makeUrl("base/router.template").toString();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        final Parameter routerTemplateUrlParm = new Parameter().withParameterKey("NetworkTemplateUrl").withParameterValue(networkTemplateUrl);
        final Parameter networkTemplateUrlParm = new Parameter().withParameterKey("RouterTemplateUrl").withParameterValue(routerTemplateUrl);
        return Arrays.<Parameter>asList(new Parameter[]{bucketNameParm, projectNameParm, availabilityZonesParm, routerTemplateUrlParm, networkTemplateUrlParm});
    }
}
