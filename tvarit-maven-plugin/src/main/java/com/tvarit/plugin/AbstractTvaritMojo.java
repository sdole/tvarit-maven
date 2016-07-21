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

import com.tvarit.plugin.env.TvaritEnvironment;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Created by Sachin Dole on 6/26/2016.
 */
public abstract class AbstractTvaritMojo extends AbstractMojo {
    @Parameter(name = "artifact-bucket-name")
    private String artifactBucketName;
    @Parameter(name = "aws-auth-profile")
    private String awsAuthProfile;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        TvaritEnvironment.init(this);
    }

    public String getArtifactBucketName() {
        return artifactBucketName;
    }

    public String getAwsAuthProfile() {
        return awsAuthProfile;
    }
}
