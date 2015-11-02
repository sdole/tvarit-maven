package com.tvarit.plugin;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by sachi_000 on 3/11/14.
 */
public class Address {

    @JsonProperty("deployment")
    private String deployment;

    public String getDeployment() {
        return deployment;
    }

    public void setDeployment(String deployment) {
        this.deployment = deployment;
    }

    /*public List<String> getServerGroup() {
        return serverGroup;
    }

    public void setServerGroup(List<String> serverGroup) {
        this.serverGroup = serverGroup;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }*/
}
