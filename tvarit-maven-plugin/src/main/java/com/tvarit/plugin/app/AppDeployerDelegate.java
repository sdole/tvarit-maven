package com.tvarit.plugin.app;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.tvarit.plugin.env.TvaritEnvironment;
import org.apache.maven.plugin.MojoExecutionException;

class AppDeployerDelegate {
    void deploy() throws MojoExecutionException {
        AmazonS3Client s3Client = TvaritEnvironment.getInstance().getAmazonS3Client();
        final PutObjectRequest putObjectRequest = new DeployPutRequestMaker().makePutRequest();
        s3Client.putObject(putObjectRequest);
    }
}
