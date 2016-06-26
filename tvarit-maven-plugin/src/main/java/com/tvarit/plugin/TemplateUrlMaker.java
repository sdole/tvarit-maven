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
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;

import java.net.MalformedURLException;
import java.net.URL;

public class TemplateUrlMaker {
    @Deprecated
    public URL makeUrl(MavenProject project, String fileName) throws MalformedURLException {
        Plugin tvaritMavenPlugin = project.getPluginManagement().getPluginsAsMap().get("io.tvarit:tvarit-maven-plugin");
        if (tvaritMavenPlugin == null)
            tvaritMavenPlugin = (Plugin) project.getPluginArtifactMap().get("io.tvarit:tvarit-maven-plugin");
        final String groupId = tvaritMavenPlugin.getGroupId();
        final String artifactId = tvaritMavenPlugin.getArtifactId();
        final String version = tvaritMavenPlugin.getVersion();
        final String infraTemplateS3Url = "https://s3.amazonaws.com/tvarit/default/" + groupId + "/" + artifactId + "/" + version + "/" + fileName;
        return new URL(infraTemplateS3Url);
    }

    public URL makeUrl(String fileName) throws MalformedURLException {
        return this.makeUrl(TvaritEnvironment.getInstance().getMavenProject(),fileName);
    }
}
