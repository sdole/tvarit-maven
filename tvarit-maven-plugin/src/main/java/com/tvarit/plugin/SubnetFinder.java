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

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeSubnetsRequest;
import com.amazonaws.services.ec2.model.DescribeSubnetsResult;
import com.amazonaws.services.ec2.model.Filter;
import org.apache.maven.project.MavenProject;

/**
 * Created by Sachin Dole on 3/27/2016.
 */
public class SubnetFinder {
    public void find(MavenProject project, AmazonEC2Client amazonEC2Client, String projectName, final StringBuilder publicSubnetIdBuilder, final StringBuilder publicSubnetAzsBuilder,final String subnetType) {


        final DescribeSubnetsRequest describeAppSubnetsRequest = new DescribeSubnetsRequest();
        final Filter subnetFilter = new Filter().withName("tag-key").withValues(projectName + ":"+subnetType);
        describeAppSubnetsRequest.withFilters(subnetFilter);
        final DescribeSubnetsResult describeSubnetsResult = amazonEC2Client.describeSubnets(describeAppSubnetsRequest);
        describeSubnetsResult.getSubnets().stream().forEach(eachSubnet -> {
            publicSubnetAzsBuilder.append(eachSubnet.getAvailabilityZone()).append(",");
            publicSubnetIdBuilder.append(eachSubnet.getSubnetId()).append(",");
        });
        publicSubnetIdBuilder.deleteCharAt(publicSubnetIdBuilder.length() - 1);
        publicSubnetAzsBuilder.deleteCharAt(publicSubnetAzsBuilder.length() - 1);
    }
}
