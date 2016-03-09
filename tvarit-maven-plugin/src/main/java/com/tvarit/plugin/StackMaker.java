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
import com.amazonaws.services.cloudformation.model.*;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

/**
 * Created by sachi_000 on 3/6/2016.
 */
public class StackMaker {
    public Stack makeStack(CreateStackRequest createStackRequest, AmazonCloudFormationClient amazonCloudFormationClient, Log log) throws MojoFailureException {

        CreateStackResult createStackResult = amazonCloudFormationClient.createStack(createStackRequest);
        final String stackName = createStackRequest.getStackName();
        DescribeStacksResult describeStacksResult = amazonCloudFormationClient.describeStacks(new DescribeStacksRequest().withStackName(stackName));
        while (describeStacksResult.getStacks().get(0).getStackStatus().equals(StackStatus.CREATE_IN_PROGRESS.toString())) {
            try {
                log.info("Awaiting stack create completion!");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                log.error(e);
                throw new RuntimeException(e);
            }
            describeStacksResult = amazonCloudFormationClient.describeStacks(new DescribeStacksRequest().withStackName(stackName));
        }
        final Stack stack = describeStacksResult.getStacks().get(0);

        final String stackStatus = stack.getStackStatus();
        if (!stackStatus.equals(StackStatus.CREATE_COMPLETE.toString())) {
            throw new MojoFailureException("Could not create infrastructure. Stack Status is: " + stackStatus + ". Please review details on the AWS console and open a new github issue on https://github.com/sdole/tvarit-maven/issues/new that is needed.");
        }
        return stack;
    }
}
