package com.tvarit.plugin;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.opsworks.AWSOpsWorksClient;

/**
 * Created by sachi_000 on 9/28/2015.
 */
public class InfrastructureCreator {
    //    @Inject
    //TODO inject this instead of direct instantiation
    private StackCreator stackCreator = new StackCreator();

    //    @Inject
    private LayerCreator layerCreator = new LayerCreator();
    private VpcCreator vpcCreator = new VpcCreator();
    private SubnetCreator subnetCreator = new SubnetCreator();
    public InfrastructureIds create(TvaritMojo tvaritMojo, AWSOpsWorksClient awsOpsWorksClient, AmazonEC2Client amazonEc2Client, String vpcName, String subnetName, String stackName, String layerName, String roleArn, String instanceProfileArn) {
        String vpcId = vpcCreator.create(amazonEc2Client, vpcName);
        String subnetId = subnetCreator.create(amazonEc2Client, vpcId, subnetName);
        final String stackId = stackCreator.create(awsOpsWorksClient, stackName, tvaritMojo, roleArn, layerName, instanceProfileArn, vpcId, subnetId);
        final String layerId = layerCreator.create(awsOpsWorksClient, layerName, tvaritMojo, stackId);
        return new InfrastructureIds(vpcId,subnetId,stackId,layerId);
    }
}
