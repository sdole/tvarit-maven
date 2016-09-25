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

package com.tvarit.plugin;

import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.ec2.AmazonEC2Client;
import org.apache.maven.project.MavenProject;

import java.util.Arrays;
import java.util.List;

public class AsgParameterMaker {
    public List<Parameter> make(AmazonEC2Client amazonEC2Client, AmazonCloudFormationClient amazonCloudFormationClient, MavenProject project, String projectName, String lambdaCodeS3Key, String lambdaCodeS3Bucket) {
        final RolesFinder rolesFinder = new RolesFinder(projectName, amazonCloudFormationClient);
        final String tvaritRoleOutput = rolesFinder.findRoleArn();
        final String tvaritInstanceProfileOutput = rolesFinder.findInstanceProfileArn();

        String vpcId = new VpcFinder().find(amazonEC2Client, projectName);
        StringBuilder publicSubnetIdBuilder = new StringBuilder();
        StringBuilder publicSubnetAzsBuilder = new StringBuilder();
        new SubnetFinder().find(project, amazonEC2Client, projectName, publicSubnetIdBuilder, publicSubnetAzsBuilder, "appSubnet");
        final String publicSubnets = publicSubnetIdBuilder.toString();
        final String publicSubnetAzs = publicSubnetAzsBuilder.toString();
        StringBuilder privateSubnetIdBuilder = new StringBuilder();
        StringBuilder privateSubnetAzsBuilder = new StringBuilder();
        new SubnetFinder().find(project, amazonEC2Client, projectName, privateSubnetIdBuilder, privateSubnetAzsBuilder, "db");
        final String privateSubnets = privateSubnetIdBuilder.toString();
        final String privateSubnetAzs = privateSubnetAzsBuilder.toString();

        final com.amazonaws.services.cloudformation.model.Parameter projectNameParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("projectName").withParameterValue(projectName);
        final com.amazonaws.services.cloudformation.model.Parameter publicSubnetsParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("publicSubnets").withParameterValue(publicSubnets);
        final com.amazonaws.services.cloudformation.model.Parameter privateSubnetsParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("privateSubnets").withParameterValue(privateSubnets);
        final com.amazonaws.services.cloudformation.model.Parameter availabilityZonesParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("availabilityZones").withParameterValue(publicSubnetAzs);
        final com.amazonaws.services.cloudformation.model.Parameter vpcParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("vpc").withParameterValue(vpcId);
        final com.amazonaws.services.cloudformation.model.Parameter healthCheckAbsoluteUrlParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("healthCheckAbsoluteUrl").withParameterValue("/tvarit/healthCheck.html");
        final com.amazonaws.services.cloudformation.model.Parameter tvaritRoleParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("tvaritRoleArn").withParameterValue(tvaritRoleOutput);
        final com.amazonaws.services.cloudformation.model.Parameter tvaritInstanceProfileParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("tvaritInstanceProfile").withParameterValue(tvaritInstanceProfileOutput);
        final com.amazonaws.services.cloudformation.model.Parameter tvaritBucketNameParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("bucketName").withParameterValue("tvarit");
        final com.amazonaws.services.cloudformation.model.Parameter tvaritLambdaCodeS3KeyParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("lambdaCodeS3Key").withParameterValue(lambdaCodeS3Key);
        final com.amazonaws.services.cloudformation.model.Parameter tvaritLambdaCodeS3BucketParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("lambdaCodeS3Bucket").withParameterValue(lambdaCodeS3Bucket);
        return Arrays.asList(projectNameParameter, publicSubnetsParameter, privateSubnetsParameter, availabilityZonesParameter, vpcParameter, healthCheckAbsoluteUrlParameter, tvaritRoleParameter, tvaritInstanceProfileParameter, tvaritBucketNameParameter, tvaritLambdaCodeS3KeyParameter, tvaritLambdaCodeS3BucketParameter);
    }
}
