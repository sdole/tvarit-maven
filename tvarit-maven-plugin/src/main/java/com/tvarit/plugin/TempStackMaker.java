package com.tvarit.plugin;

import org.apache.maven.project.MavenProject;

import java.net.MalformedURLException;

class TempStackMaker {
     void makeTempStack(MavenProject project) {
         try {
             String templateUrl = new TemplateUrlMaker().makeUrl(project, "autoscaling.template").toString();
         } catch (MalformedURLException e) {
             throw new RuntimeException(e);
         }

     }
}
