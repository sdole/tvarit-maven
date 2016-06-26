package com.tvarit.plugin.app;

import com.tvarit.plugin.env.TvaritEnvironment;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "deploy-app")
public class AppDeployerMojo extends AbstractMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        TvaritEnvironment.init(this);
        new AppDeployerDelegate().deploy();
    }
}
