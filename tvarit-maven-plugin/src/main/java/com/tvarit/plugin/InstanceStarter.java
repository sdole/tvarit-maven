package com.tvarit.plugin;

import com.amazonaws.services.opsworks.AWSOpsWorksClient;
import com.amazonaws.services.opsworks.model.StartInstanceRequest;

/**
 * Created by sachi_000 on 9/30/2015.
 */
public class InstanceStarter {
    public void start(AWSOpsWorksClient awsOpsWorksClient, String instanceId) {
        final StartInstanceRequest startInstanceRequest = new StartInstanceRequest();
        startInstanceRequest.withInstanceId(instanceId);
        awsOpsWorksClient.startInstance(startInstanceRequest);
    }
}
