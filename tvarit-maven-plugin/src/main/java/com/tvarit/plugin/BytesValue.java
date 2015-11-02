package com.tvarit.plugin;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by sachi_000 on 3/11/14.
 */
public class BytesValue {
    @JsonProperty("BYTES_VALUE")
    private String bytesValue;

    public String getBytesValue() {
        return bytesValue;
    }

    public void setBytesValue(String bytesValue) {
        this.bytesValue = bytesValue;
    }
}
