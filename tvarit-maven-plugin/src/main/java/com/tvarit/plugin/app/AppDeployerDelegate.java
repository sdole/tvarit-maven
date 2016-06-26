package com.tvarit.plugin.app;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.tvarit.plugin.env.TvaritEnvironment;

class AppDeployerDelegate {
    void deploy() {
        AmazonS3Client s3Client = TvaritEnvironment.getInstance().getAmazonS3Client();
        final PutObjectRequest putObjectRequest = new DeployPutRequestMaker().makePutRequest();
        s3Client.putObject(putObjectRequest);
    }
}
