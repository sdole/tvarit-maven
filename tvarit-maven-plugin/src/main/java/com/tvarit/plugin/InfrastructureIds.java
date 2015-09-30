package com.tvarit.plugin;

/**
 * Created by sachi_000 on 9/29/2015.
 */
public class InfrastructureIds {
    private final String vpcId;
    private final String subnetId;
    private final String stackId;
    private final String layerId;

    public InfrastructureIds(String vpcId, String subnetId, String stackId, String layerId) {

        this.vpcId = vpcId;
        this.subnetId = subnetId;
        this.stackId = stackId;
        this.layerId = layerId;
    }

    public String getVpcId() {
        return vpcId;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public String getLayerId() {
        return layerId;
    }

    public String getStackId() {
        return stackId;
    }
}
