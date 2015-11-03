package com.tvarit.plugin;

import com.amazonaws.services.opsworks.AWSOpsWorksClient;
import com.amazonaws.services.opsworks.model.DescribeInstancesRequest;
import com.amazonaws.services.opsworks.model.DescribeInstancesResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by sachi_000 on 10/27/2015.
 */
public class WarAppDeployer {

    public void deploy(MavenProject project, AWSOpsWorksClient awsOpsWorksClient, String instanceId) {
        final DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
        describeInstancesRequest.withInstanceIds(instanceId);
        final DescribeInstancesResult describeInstancesResult = awsOpsWorksClient.describeInstances(describeInstancesRequest);
        final String publicIpAddress = describeInstancesResult.getInstances().get(0).getPublicIp();
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getByName(publicIpAddress);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        HttpHost target = new HttpHost(inetAddress, 9990, "http");
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(target.getHostName(), target.getPort()),
                new UsernamePasswordCredentials("wildfly", "wildfly"));
        CloseableHttpClient aDefault = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
        AuthCache authCache = new BasicAuthCache();
        DigestScheme digestAuth = new DigestScheme();
        // Suppose we already know the realm name
        digestAuth.overrideParamter("realm", "ManagementRealm");
        // Suppose we already know the expected nonce value
//        digestAuth.overrideParamter("nonce", "whatever");
        authCache.put(target, digestAuth);

        // Add AuthCache to the execution context
        HttpClientContext localContext = HttpClientContext.create();
        localContext.setAuthCache(authCache);
//        HttpPost request = new HttpPost(new URI(target.getSchemeName(),"ec2-user", target.getHostName(),target.getPort(), "/management/add-content","",""));
        HttpPost request = new HttpPost("/management/add-content");
//        FileEntity fileEntity = new FileEntity(warFile);
        final File warFile = project.getArtifact().getFile();

        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create().addBinaryBody("file", warFile);
        request.setEntity(multipartEntityBuilder.build());
        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = aDefault.execute(target, request, localContext);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        HttpEntity httpResponseEntity = httpResponse.getEntity();
        StatusLine statusLine = httpResponse.getStatusLine();
        if (statusLine.getStatusCode() >= 300) {
            throw new RuntimeException(new HttpResponseException(
                    statusLine.getStatusCode(),
                    statusLine.getReasonPhrase()));
        }
        if (httpResponseEntity == null) {
            throw new RuntimeException(new ClientProtocolException("Response contains no content"));
        }
        String responseText = null;
        try {
            responseText = EntityUtils.toString(httpResponseEntity);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            EntityUtils.consume(httpResponseEntity);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        log.info("Uploaded and received: " + responseText);
        ObjectMapper codec = new ObjectMapper();
        ObjectNode treeNode;
        try {
            treeNode = (ObjectNode) codec.readTree(responseText);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int waitTimeForUploadedFileToSettle = 30000;
        try {
//            log.info("Waiting " + TimeUnit.SECONDS.convert(waitTimeForUploadedFileToSettle, TimeUnit.MILLISECONDS) + " seconds for uploaded file to settle.");
            Thread.sleep(waitTimeForUploadedFileToSettle);
        } catch (InterruptedException e) {
            throw new RuntimeException("Thread interrupted when waiting for jboss to accept new deployable war file.", e);
        }
//        log.info("Done waiting for uploaded file to settle");

        DeployRequestMaker deployRequestMaker = new DeployRequestMaker();
        StringEntity entity = null;
        try {
            entity = deployRequestMaker.make(treeNode, warFile.getName());
        } catch (JsonProcessingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        HttpPost deployRequest = new HttpPost("/management");
        deployRequest.setEntity(entity);
        CloseableHttpResponse deployResponse = null;
        try {
            deployResponse = aDefault.execute(target, deployRequest, localContext);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        HttpEntity deployResponseEntity = deployResponse.getEntity();
        String deployResponseString = null;
        try {
            deployResponseString = EntityUtils.toString(deployResponseEntity);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (deployResponse.getStatusLine().getStatusCode() > HttpStatus.SC_OK) {
            throw new RuntimeException(new MojoExecutionException("Could not deploy to server. due to http error code: " + deployResponse.getStatusLine().getStatusCode() + " " + deployResponse.getStatusLine().getReasonPhrase()));
        }
        try {
            EntityUtils.consume(deployResponseEntity);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
