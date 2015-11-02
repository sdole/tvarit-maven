package com.tvarit.plugin;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by sachi_000 on 3/11/14.
 */
class DeployJson {

    @JsonProperty("content")
    private List<Hash> content;

    @JsonProperty("address")
    private List<Address> address;

    @JsonProperty("operation")
    private String operation = "add";
    @JsonProperty("enabled")
    private String enabled = "true";

    public List<Hash> getContent() {
        return content;
    }

    public void setContent(List<Hash> content) {
        this.content = content;
    }

    public List<Address> getAddress() {
        return address;
    }

    public void setAddress(List<Address> address) {
        this.address = address;
    }

}
