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

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.Capability;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.net.MalformedURLException;

@Mojo(name = "make-infrastructure")
public class NewInfrastructureMojo extends AbstractMojo {
    @Parameter(required = true, readonly = true, property = "mySecretKey")
    private String secretKey;
    @Parameter(required = true, readonly = true, property = "myAccessKey")
    private String accessKey;
    @Parameter
    private String templateUrl;
    @Parameter(required = true)
    private String projectName;
    @Parameter(required = true)
    private String domainName;
    @Parameter(required = true)
    private String bucketName;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().debug("Starting " + this.getClass().getSimpleName() + " execution ");
        final BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        AmazonCloudFormationClient amazonCloudFormationClient = new AmazonCloudFormationClient(awsCredentials);
        final com.amazonaws.services.cloudformation.model.Parameter domainNameParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("domainName").withParameterValue(this.domainName);
        final com.amazonaws.services.cloudformation.model.Parameter projectNameParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("projectName").withParameterValue(this.projectName);
        final com.amazonaws.services.cloudformation.model.Parameter bucketNameParameter = new com.amazonaws.services.cloudformation.model.Parameter().withParameterKey("bucketName").withParameterValue(this.bucketName);
        final String template;
        final MavenProject project = (MavenProject) this.getPluginContext().getOrDefault("project", null);

        if (templateUrl == null) {
            try {
                templateUrl  = new TemplateUrlMaker().makeUrl(project,"vpc-infra.template").toString();
            } catch (MalformedURLException e) {
                throw new MojoExecutionException("Could not create default url for templates. Please open an issue on github.",e);
            }
        }
        final CreateStackRequest createStackRequest =
                new CreateStackRequest().
                        withCapabilities(Capability.CAPABILITY_IAM).
                        withStackName(projectName + "-infra").
                        withParameters(domainNameParameter, projectNameParameter, bucketNameParameter).
                        withTemplateURL(templateUrl);
        new StackMaker().makeStack(createStackRequest, amazonCloudFormationClient, getLog());
        getLog().info("Finished completing stack");

    }

}
