package com.tvarit.plugin;

/**
 * Created by sachi_000 on 9/21/2015.
 */
public class VpcIds {
    private String vpcId;
    private String subnetId;

    public VpcIds(String vpcId, String subnetId) {

        this.vpcId = vpcId;
        this.subnetId = subnetId;
    }


    public String getSubnetId() {
        return subnetId;
    }

    public String getVpcId() {
        return vpcId;
    }

}
