package com.tvarit.plugin;

import com.amazonaws.auth.AWSCredentials;

import javax.inject.Provider;

/**
 * Created by sachi_000 on 8/19/2015.
 */
public class CredsProvider implements Provider<AWSCredentials> {
    @Override
    public AWSCredentials get() {
        return null;
    }
}
