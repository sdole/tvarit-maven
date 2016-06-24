package com.tvarit.plugin.vpc;

import com.amazonaws.services.cloudformation.model.Parameter;
import com.tvarit.plugin.MakeVpcMojo;
import com.tvarit.plugin.env.TvaritEnvironment;
import edu.emory.mathcs.backport.java.util.Arrays;
import org.apache.maven.project.MavenProject;

import java.util.List;

class MakeVpcParameterMaker {
    List<Parameter> make() {
        String projectName = TvaritEnvironment.getInstance().getProjectName();
        String bucketName = TvaritEnvironment.getInstance().<MakeVpcMojo>getMojo().getBucketName();
        String availabilityZones = TvaritEnvironment.getInstance().<MakeVpcMojo>getMojo().getAvailabilityZones();
        final com.amazonaws.services.cloudformation.model.Parameter bucketNameParm = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("TvaritBucketNameParm").withParameterValue(bucketName);
        final com.amazonaws.services.cloudformation.model.Parameter projectNameParm = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("ProjectNameParm").withParameterValue(projectName);
        final com.amazonaws.services.cloudformation.model.Parameter availabilityZonesParm = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("AvailabilityZones").withParameterValue(availabilityZones);
        return Arrays.<Parameter>asList(new Parameter[]{bucketNameParm, projectNameParm, availabilityZonesParm});
    }
}
