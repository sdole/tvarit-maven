package com.tvarit.plugin.app;

import com.tvarit.plugin.AbstractTvaritMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "deploy-app")
public class AppDeployerMojo extends AbstractTvaritMojo {

    @Parameter(name = "ssh-key-name")
    private String sshKeyName;
    @Parameter(name = "db-version")
    private String dbVersion;
    @Parameter(name = "health-check-url")
    private String healthCheckUrl;
    @Parameter(name = "context-config-url", defaultValue = "app/web_app.xml", required = false)
    private String contextConfigUrl;
    @Parameter(name = "context-root", defaultValue = "/", required = false)
    private String contextRoot;
    @Parameter(name = "app-fqdn")
    private String appFqdn;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        super.execute();
        new AppDeployerDelegate().deploy();
    }

    public String getSshKeyName() {
        return sshKeyName;
    }

    public String getDbVersion() {
        return dbVersion;
    }

    public String getHealthCheckUrl() {
        return healthCheckUrl;
    }

    public String getContextConfigUrl() {
        return contextConfigUrl;
    }

    public String getContextRoot() {
        return contextRoot;
    }

    public String getAppFqdn() {
        return appFqdn;
    }
}
