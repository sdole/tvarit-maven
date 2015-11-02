package com.tvarit.plugin;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by sachi_000 on 3/11/14.
 */
public class Hash {

    @JsonProperty("hash")
    private BytesValue hash;

    public BytesValue getHash() {
        return hash;
    }

    public void setHash(BytesValue hash) {
        this.hash = hash;
    }
}
