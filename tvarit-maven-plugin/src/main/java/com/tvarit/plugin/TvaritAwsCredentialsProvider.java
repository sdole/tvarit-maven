package com.tvarit.plugin;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;

/**
 * Created by sachi_000 on 8/19/2015.
 */
public class TvaritAwsCredentialsProvider implements AWSCredentialsProvider {
    @Override
    public AWSCredentials getCredentials() {
        return null;
    }

    @Override
    public void refresh() {

    }
}
