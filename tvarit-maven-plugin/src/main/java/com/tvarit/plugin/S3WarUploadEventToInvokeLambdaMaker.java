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

import com.amazonaws.services.cloudformation.model.Output;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Sachin Dole on 3/27/2016.
 * To create S3 event to execute lambda
 */
class S3WarUploadEventToInvokeLambdaMaker {
    void make(AmazonS3Client amazonS3Client, String bucketName, Stack stack) {
        final List<Output> outputs = stack.getOutputs();
        final String lambdaFunctionArn = outputs.stream().filter(output -> output.getOutputKey().equals("LambdaFunctionArn")).findFirst().get().getOutputValue();
        final BucketNotificationConfiguration notificationConfiguration = new BucketNotificationConfiguration();
        final HashMap<String, NotificationConfiguration> configurations = new HashMap<>();
        final LambdaConfiguration lambdaConfiguration = new LambdaConfiguration(lambdaFunctionArn);
        final HashSet<String> events = new HashSet<>();
        events.add("s3:ObjectCreated:*");
        lambdaConfiguration.setEvents(events);
        final com.amazonaws.services.s3.model.Filter notificationFilter = new com.amazonaws.services.s3.model.Filter();
        final S3KeyFilter s3KeyFilter = new S3KeyFilter();
        notificationFilter.withS3KeyFilter(s3KeyFilter);
        s3KeyFilter.withFilterRules(
                new FilterRule().withName("suffix").withValue(".war"),
                new FilterRule().withName("prefix").withValue("deployables")
        );
        lambdaConfiguration.setFilter(notificationFilter);
        configurations.put("warUploaded", lambdaConfiguration);
        notificationConfiguration.setConfigurations(configurations);
        final SetBucketNotificationConfigurationRequest setBucketNotificationConfigurationRequest = new SetBucketNotificationConfigurationRequest(bucketName, notificationConfiguration);
        amazonS3Client.setBucketNotificationConfiguration(setBucketNotificationConfigurationRequest);

    }
}
