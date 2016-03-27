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

import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;

/**
 * Created by Sachin Dole on 3/27/2016.
 */
public class LambdaS3BucketKeyMaker {
    public String makeKey(MavenProject project) {
        Plugin tvaritMavenPlugin = project.getPluginManagement().getPluginsAsMap().get("io.tvarit:tvarit-maven-plugin");
        if (tvaritMavenPlugin == null)
            tvaritMavenPlugin = (Plugin) project.getPluginArtifactMap().get("io.tvarit:tvarit-maven-plugin");
        final String groupId = tvaritMavenPlugin.getGroupId();
        final String artifactId = tvaritMavenPlugin.getArtifactId();
        final String version = tvaritMavenPlugin.getVersion();
        return "default/" + groupId + "/" + artifactId + "/" + version + "/lambda/" + "deployNewWar.zip";
    }
}
