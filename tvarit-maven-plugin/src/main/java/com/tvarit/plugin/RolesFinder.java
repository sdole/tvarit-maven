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
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;

import java.util.stream.Collectors;

/**
 * Created by Sachin Dole on 3/27/2016.
 */
public class RolesFinder {
    public RolesFinder(String projectName, AmazonCloudFormationClient amazonCloudFormationClient) {
        final DescribeStacksRequest describeStacksRequest = new DescribeStacksRequest();
        describeStacksRequest.withStackName(projectName + "-infra");
        describeStacksResult = amazonCloudFormationClient.describeStacks(describeStacksRequest);

    }

    private final DescribeStacksResult describeStacksResult;

    public String findRoleArn() {
        return describeStacksResult.getStacks().get(0).getOutputs().stream().filter(output -> "TvaritRole".equals(output.getOutputKey())).collect(Collectors.toList()).get(0).getOutputValue();
    }

    public String findInstanceProfileArn() {
        return describeStacksResult.getStacks().get(0).getOutputs().stream().filter(output -> "TvaritInstanceProfile".equals(output.getOutputKey())).collect(Collectors.toList()).get(0).getOutputValue();
    }
}
