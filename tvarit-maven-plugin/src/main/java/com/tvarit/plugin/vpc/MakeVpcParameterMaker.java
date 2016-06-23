package com.tvarit.plugin.vpc;

import com.amazonaws.services.cloudformation.model.Parameter;
import com.tvarit.plugin.MakeVpcMojo;
import com.tvarit.plugin.env.TvaritEnvironment;
import edu.emory.mathcs.backport.java.util.Arrays;

import java.util.List;

class MakeVpcParameterMaker {
    List<Parameter> make() {
        String projectName = TvaritEnvironment.getInstance().<MakeVpcMojo>getMojo().getProjectName();
        String domainName = TvaritEnvironment.getInstance().<MakeVpcMojo>getMojo().getDomainName();
        String bucketName = TvaritEnvironment.getInstance().<MakeVpcMojo>getMojo().getBucketName();
        final com.amazonaws.services.cloudformation.model.Parameter bucketNameParm = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("TvaritBucketNameParm").withParameterValue(domainName);
        final com.amazonaws.services.cloudformation.model.Parameter projectNameParm = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("ProjectNameParm").withParameterValue(projectName);
        final com.amazonaws.services.cloudformation.model.Parameter tvaritBucketNameParm = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("TvaritBucketNameParm").withParameterValue(bucketName);
        return Arrays.<Parameter>asList(new Parameter[]{bucketNameParm, projectNameParm, tvaritBucketNameParm});
    }
}
