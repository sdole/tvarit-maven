package com.tvarit.plugin;

import com.amazonaws.services.opsworks.AWSOpsWorksClient;
import com.amazonaws.services.opsworks.model.DescribeInstancesRequest;
import com.amazonaws.services.opsworks.model.DescribeInstancesResult;

/**
 * Created by sachi_000 on 10/27/2015.
 */
public class WaitTillInstanceOnline {
    private static final int WAIT_BETWEEN_STATUS_CHECK = 120000;

    public void waitTillInstanceOnline(AWSOpsWorksClient awsOpsWorksClient, String instanceId) {
        boolean isInstanceOnline = false;
        while (!isInstanceOnline) {
            final DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
            describeInstancesRequest.withInstanceIds(instanceId);
            final DescribeInstancesResult describeInstancesResult = awsOpsWorksClient.describeInstances(describeInstancesRequest);
            final String status = describeInstancesResult.getInstances().get(0).getStatus();
            isInstanceOnline = "online".equals(status);
            try {
                Thread.sleep(WAIT_BETWEEN_STATUS_CHECK);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
