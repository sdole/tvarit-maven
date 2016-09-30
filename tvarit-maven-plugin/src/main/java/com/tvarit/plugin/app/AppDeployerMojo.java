package com.tvarit.plugin.app;

import com.tvarit.plugin.AbstractTvaritMojo;
import com.tvarit.plugin.env.TvaritEnvironment;
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
    @Parameter(name = "db-name")
    private String dbName;
    @Parameter(name = "db-username", defaultValue = "fastup")
    private String dbUsername;
    @Parameter(name = "db-password", defaultValue = "fastup123")
    private String dbPassword;

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

    public String getDbName() {
        if (dbName == null || dbName.length() == 0) {
            final TvaritEnvironment environment = TvaritEnvironment.getInstance();
            dbName = (environment.getProjectNameCapitalized() + "Db");
        }
        return dbName;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public String getDbPassword() {
        return dbPassword;
    }
}
