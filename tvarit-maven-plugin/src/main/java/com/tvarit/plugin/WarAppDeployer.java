package com.tvarit.plugin;

import com.amazonaws.services.opsworks.AWSOpsWorksClient;
import org.apache.maven.project.MavenProject;

/**
 * Created by sachi_000 on 10/27/2015.
 */
public class WarAppDeployer {

    private WaitTillInstanceOnline waitTillInstanceOnline = new  WaitTillInstanceOnline();

    public void deploy(MavenProject project, AWSOpsWorksClient awsOpsWorksClient, String instanceId) {
        waitTillInstanceOnline.waitTillInstanceOnline(awsOpsWorksClient,instanceId);
    }
}
