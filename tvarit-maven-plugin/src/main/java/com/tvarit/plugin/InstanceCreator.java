package com.tvarit.plugin;

import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.opsworks.AWSOpsWorksClient;
import com.amazonaws.services.opsworks.model.*;

/**
 * Created by sachi_000 on 9/28/2015.
 */
public class InstanceCreator {
    public String create(TvaritMojo tvaritMojo, AWSOpsWorksClient awsOpsWorksClient, InfrastructureIds infrastructureIds) {
        final CreateInstanceRequest createInstanceRequest = new CreateInstanceRequest();
        final BlockDeviceMapping blockDeviceMapping = new BlockDeviceMapping();
        final EbsBlockDevice ebsBlockDevice = new EbsBlockDevice();
        ebsBlockDevice
                .withVolumeSize(32)
                .withDeleteOnTermination(true)
                .withVolumeType(VolumeType.Gp2);
        blockDeviceMapping.withEbs(ebsBlockDevice).withDeviceName("ROOT_DEVICE");
        createInstanceRequest.
                withStackId(infrastructureIds.getStackId()).
                withLayerIds(infrastructureIds.getLayerId()).
                withInstanceType(InstanceType.M3Medium.toString()).
                withRootDeviceType(RootDeviceType.Ebs).
                withSubnetId(infrastructureIds.getSubnetId()).
                withBlockDeviceMappings(blockDeviceMapping)
        ;
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final CreateInstanceResult createInstanceResult = awsOpsWorksClient.createInstance(createInstanceRequest);
        AssociateElasticIpRequest associateElasticIpRequest = new AssociateElasticIpRequest();
        final String instanceId = createInstanceResult.getInstanceId();
        associateElasticIpRequest.withElasticIp(infrastructureIds.getIpAddress()).withInstanceId(instanceId);
        awsOpsWorksClient.associateElasticIp(associateElasticIpRequest);
        tvaritMojo.getLog().debug("started instance with id: " + instanceId);
        return instanceId;
    }
}
