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
}
