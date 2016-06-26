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
import com.tvarit.plugin.env.TvaritEnvironment;

import java.io.File;
import java.util.HashMap;

class DeployPutRequestMaker {
    PutObjectRequest makePutRequest() {
        final TvaritEnvironment tvaritEnvironment = TvaritEnvironment.getInstance();
        tvaritEnvironment.<AppDeployerMojo>getMojo().getArtifactBucketName();
        final File warFile = tvaritEnvironment.getMavenProject().getArtifact().getFile();
        String projectArtifactId = tvaritEnvironment.getMavenProject().getArtifactId();
        String projectVersion = tvaritEnvironment.getMavenProject().getVersion();
        final String projectGroupId = tvaritEnvironment.getMavenProject().getGroupId();
        final String key = "deployables/" + projectGroupId + "/" + projectArtifactId + "/" + projectVersion + "/" + warFile.getName();

        final String bucketName = tvaritEnvironment.<AppDeployerMojo>getMojo().getArtifactBucketName();
        final PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, warFile);
        final ObjectMetadata metadata = new ObjectMetadata();
        final HashMap<String, String> userMetadata = new HashMap<>();
        userMetadata.put("project_name", tvaritEnvironment.getProjectName());
        userMetadata.put("private_key_name", tvaritEnvironment.<AppDeployerMojo>getMojo().getSshKeyName());
        metadata.setUserMetadata(userMetadata);
        putObjectRequest.withMetadata(metadata);
        return putObjectRequest;
    }
}
