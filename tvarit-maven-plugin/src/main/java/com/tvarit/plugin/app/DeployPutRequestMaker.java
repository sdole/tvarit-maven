/*
 * Tvarit is an AWS DevOps Automation Tool for JEE applications.
 * See http://www.tvarit.io
 *     Copyright (C) 2016. Sachin Dole.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.tvarit.plugin.app;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.tvarit.plugin.TemplateUrlMaker;
import com.tvarit.plugin.env.TvaritEnvironment;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

class DeployPutRequestMaker {
    PutObjectRequest makePutRequest() throws MojoExecutionException {
        final TvaritEnvironment tvaritEnvironment = TvaritEnvironment.getInstance();
        tvaritEnvironment.<AppDeployerMojo>getMojo().getArtifactBucketName();
        final File warFile = tvaritEnvironment.getMavenProject().getArtifact().getFile();
        String projectArtifactId = tvaritEnvironment.getMavenProject().getArtifactId();
        String projectVersion = tvaritEnvironment.getMavenProject().getVersion();
        final String projectGroupId = tvaritEnvironment.getMavenProject().getGroupId();
        final String key = "deployables/" + projectGroupId + "/" + projectArtifactId + "/" + projectVersion + "/" + warFile.getName();

        final String bucketName = tvaritEnvironment.getArtifactBucketName();
        final PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, warFile);
        final ObjectMetadata metadata = new ObjectMetadata();
        final Map<String, String> userMetadata = new HashMap<>();
        userMetadata.put("project_name", tvaritEnvironment.getProjectName());
        userMetadata.put("health_check_url", tvaritEnvironment.<AppDeployerMojo>getMojo().getHealthCheckUrl());
        userMetadata.put("private_key_name", tvaritEnvironment.<AppDeployerMojo>getMojo().getSshKeyName());
        userMetadata.put("db-version", tvaritEnvironment.<AppDeployerMojo>getMojo().getDbVersion());
        userMetadata.put("group-id", tvaritEnvironment.getMavenProject().getGroupId());
        userMetadata.put("artifact-id", tvaritEnvironment.getMavenProject().getArtifactId());
        userMetadata.put("version", tvaritEnvironment.getMavenProject().getVersion());
        userMetadata.put("app_fqdn", tvaritEnvironment.<AppDeployerMojo>getMojo().getAppFqdn());
        userMetadata.put("db-name", tvaritEnvironment.<AppDeployerMojo>getMojo().getDbName());
        userMetadata.put("db-username", tvaritEnvironment.<AppDeployerMojo>getMojo().getDbUsername());
        userMetadata.put("db-password", tvaritEnvironment.<AppDeployerMojo>getMojo().getDbPassword());
        final String contextConfigUrl = tvaritEnvironment.<AppDeployerMojo>getMojo().getContextConfigUrl();
        final URL url;
        try {
            url = new TemplateUrlMaker().makeUrl(contextConfigUrl);
        } catch (MalformedURLException e) {
            throw new MojoExecutionException("failed", e);
        }
        userMetadata.put("context_config_url", url.toString());
        final String contextRoot = tvaritEnvironment.<AppDeployerMojo>getMojo().getContextRoot();
        userMetadata.put("context_root", contextRoot.equals("/") ? "ROOT" : contextRoot);
        metadata.setUserMetadata(userMetadata);
        putObjectRequest.withMetadata(metadata);
        return putObjectRequest;
    }
}
