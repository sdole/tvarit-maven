package com.tvarit.plugin.base;

import com.amazonaws.services.cloudformation.model.Parameter;
import com.tvarit.plugin.MakeBaseInfrastructureMojo;
import com.tvarit.plugin.TemplateUrlMaker;
import com.tvarit.plugin.env.TvaritEnvironment;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class MakeBaseInfrastructureParameterMaker {
    private static final String TVARIT_BUCKET_NAME = "tvarit";

    List<Parameter> make() {
        String projectName = TvaritEnvironment.getInstance().getProjectName();
        final String artifactId = TvaritEnvironment.getInstance().getMavenProject().getArtifactId();
        final String projectVersion = TvaritEnvironment.getInstance().getMavenProject().getVersion();
        final String artifactBucketName = TvaritEnvironment.getInstance().getArtifactBucketName();
        final String availabilityZones = TvaritEnvironment.getInstance().<MakeBaseInfrastructureMojo>getMojo().getAvailabilityZones();
        final String domainName = TvaritEnvironment.getInstance().<MakeBaseInfrastructureMojo>getMojo().getDomainName();
        final Parameter bucketNameParm = new Parameter().withParameterKey("ArtifactBucketNameParm").withParameterValue(artifactBucketName);
        final Parameter projectNameParm = new Parameter().withParameterKey("ProjectNameParm").withParameterValue(projectName);
        final Parameter availabilityZonesParm = new Parameter().withParameterKey("AvailabilityZones").withParameterValue(availabilityZones);
        final Parameter elbHealthCheckAbsoluteUrlParm = new Parameter().withParameterKey("ElbHealthCheckUrl").withParameterValue("/to_be_fixed.html");
        final Parameter sshKeyParm = new Parameter().withParameterKey("SshKeyPairName").withParameterValue(TvaritEnvironment.getInstance().<MakeBaseInfrastructureMojo>getMojo().getSshKeyPairName());
        String routerTemplateUrl;
        String iamTemplateUrl;
        String networkTemplateUrl;
        String deployerLambdaTemplateUrl;
        String tvaritArtifactBucketTemplateUrl;
        String bastionHostTemplateUrl;
        String snsTopicsUrl;
        String allDeployerLambdaFunctionCodeS3Key = "default/io.tvarit/tvarit-maven-plugin/0.1.2-SNAPSHOT/lambda/tvarit-lambda.zip";
        try {
            snsTopicsUrl = new TemplateUrlMaker().makeUrl("base/sns_topics.template").toString();
            iamTemplateUrl = new TemplateUrlMaker().makeUrl("base/iam.template").toString();
            deployerLambdaTemplateUrl = new TemplateUrlMaker().makeUrl("base/deployer_lambda.template").toString();
            networkTemplateUrl = new TemplateUrlMaker().makeUrl("base/network.template").toString();
            routerTemplateUrl = new TemplateUrlMaker().makeUrl("base/router.template").toString();
            tvaritArtifactBucketTemplateUrl = new TemplateUrlMaker().makeUrl("base/artifact_bucket.template").toString();
            bastionHostTemplateUrl = new TemplateUrlMaker().makeUrl("base/bastion.template").toString();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        final Parameter routerTemplateUrlParm = new Parameter().withParameterKey("NetworkTemplateUrl").withParameterValue(networkTemplateUrl);
        final Parameter networkTemplateUrlParm = new Parameter().withParameterKey("RouterTemplateUrl").withParameterValue(routerTemplateUrl);
        final Parameter iamTemplateUrlParm = new Parameter().withParameterKey("IamTemplateUrl").withParameterValue(iamTemplateUrl);
        final Parameter bastionHostTemplateUrlParam = new Parameter().withParameterKey("BastionHostTemplateUrlParam").withParameterValue(bastionHostTemplateUrl);
        final Parameter deployerLambdaTemplateUrlParm = new Parameter().withParameterKey("DeployerLambdaTemplateUrl").withParameterValue(deployerLambdaTemplateUrl);
        final Parameter tvaritArtifactBucketS3BucketParam = new Parameter().withParameterKey("TvaritArtifactBucketTemplateUrl").withParameterValue(tvaritArtifactBucketTemplateUrl);
        final Parameter deployerLambdaFunctionCodeS3BucketParam = new Parameter().withParameterKey("DeployerLambdaFunctionCodeS3BucketParam").withParameterValue(TVARIT_BUCKET_NAME);
        final Parameter deployerLambdaFunctionCodeS3KeyParam = new Parameter().withParameterKey("DeployerLambdaFunctionCodeS3KeyParam").withParameterValue(allDeployerLambdaFunctionCodeS3Key);
        final Parameter snsTopicsUrlParam = new Parameter().withParameterKey("SnsTopicsTemplateUrl").withParameterValue(snsTopicsUrl);
        final Parameter domainNameParam = new Parameter().withParameterKey("DomainNameParam").withParameterValue(domainName);
        final ArrayList<Parameter> listOfParms = new ArrayList<>();
        listOfParms.add(bucketNameParm);
        listOfParms.add(projectNameParm);
        listOfParms.add(availabilityZonesParm);
        listOfParms.add(routerTemplateUrlParm);
        listOfParms.add(networkTemplateUrlParm);
        listOfParms.add(elbHealthCheckAbsoluteUrlParm);
        listOfParms.add(sshKeyParm);
        listOfParms.add(bastionHostTemplateUrlParam);
        listOfParms.add(domainNameParam);
        listOfParms.add(iamTemplateUrlParm);
        listOfParms.add(deployerLambdaTemplateUrlParm);
        listOfParms.add(deployerLambdaFunctionCodeS3KeyParam);
        listOfParms.add(deployerLambdaFunctionCodeS3BucketParam);
        listOfParms.add(tvaritArtifactBucketS3BucketParam);
        listOfParms.add(snsTopicsUrlParam);
        final List<String> stringifiedListOfParms = listOfParms.stream().map(parameter -> parameter.getParameterKey() + " : " + parameter.getParameterValue()).collect(Collectors.toList());
        TvaritEnvironment.getInstance().getLogger().info("Parameters for main template are: \n\t" + String.join("\n\t", stringifiedListOfParms));
        return listOfParms;
    }
}
