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
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.CreateStackResult;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Sachin Dole on 9/13/2016.
 */
@Mojo(name = "deploy-eb")
public class EbMojo extends AbstractTvaritMojo {
    private final String applicationName = "newtest2";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        super.execute();

/*
        AWSElasticBeanstalkClient ebClient = new AWSElasticBeanstalkClient();

        final CreateApplicationRequest createApplicationRequest = new CreateApplicationRequest();
        createApplicationRequest.withApplicationName(applicationName).withDescription("new app with eb");
        final CreateApplicationResult application = ebClient.createApplication(createApplicationRequest);
        final CreateApplicationVersionRequest createApplicationVersionRequest = new CreateApplicationVersionRequest();
        createApplicationVersionRequest.withDescription("ver1").withApplicationName(application.getApplication().getApplicationName()).withAutoCreateApplication(true).withProcess(true).withVersionLabel("v1");
        final CreateEnvironmentRequest createEnvironmentRequest = new CreateEnvironmentRequest();
        final CreateApplicationVersionResult applicationVersion = ebClient.createApplicationVersion(createApplicationVersionRequest);
        createEnvironmentRequest.
                withDescription("an environment").
                withVersionLabel(applicationVersion.getApplicationVersion().getVersionLabel()).
                withApplicationName(application.getApplication().getApplicationName()).
                withEnvironmentName("anenv").
                withSolutionStackName("64bit Amazon Linux 2016.03 v2.2.0 running Tomcat 8 Java 8")
        ;
        ebClient.createEnvironment(createEnvironmentRequest);*/

        AmazonCloudFormationClient cloudFormationClient = new AmazonCloudFormationClient();
        final URL url;
        try {
            url = new TemplateUrlMaker().makeUrl("eb/eb.template");
        } catch (MalformedURLException e) {
            throw new MojoExecutionException("fail", e);
        }
        final CreateStackRequest createStackRequest = new CreateStackRequest();
        createStackRequest.withStackName("testapppstack");
        createStackRequest.withTemplateURL(url.toString());
        final CreateStackResult stack = cloudFormationClient.createStack(createStackRequest);

    }
}
