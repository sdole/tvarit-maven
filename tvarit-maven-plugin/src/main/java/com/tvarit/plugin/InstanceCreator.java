package com.tvarit.plugin;

import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.opsworks.AWSOpsWorksClient;
import com.amazonaws.services.opsworks.model.CreateInstanceRequest;
import com.amazonaws.services.opsworks.model.CreateInstanceResult;
import com.amazonaws.services.opsworks.model.RootDeviceType;

/**
 * Created by sachi_000 on 9/28/2015.
 */
public class InstanceCreator {
    public void create(TvaritMojo tvaritMojo, AWSOpsWorksClient awsOpsWorksClient, InfrastructureIds infrastructureIds) {
        final CreateInstanceRequest createInstanceRequest = new CreateInstanceRequest();
        createInstanceRequest.
                withStackId(infrastructureIds.getStackId()).
                withLayerIds(infrastructureIds.getLayerId()).
                withInstanceType(InstanceType.T2Micro.toString()).
                withRootDeviceType(RootDeviceType.Ebs).
                withSubnetId(infrastructureIds.getSubnetId());
        final CreateInstanceResult instance = awsOpsWorksClient.createInstance(createInstanceRequest);
        tvaritMojo.getLog().debug("started instance with id: " + instance.getInstanceId());

    }
}
