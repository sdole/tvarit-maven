package com.tvarit.plugin;

import com.tvarit.plugin.base.MakeBaseInfrastructureDelegate;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "make-base-infrastructure")
public class MakeBaseInfrastructureMojo extends AbstractTvaritMojo {

    @Parameter(required = true, alias = "availability-zones")
    private String availabilityZones;
    @Parameter(required = true, alias = "ssh-key-pair-name")
    private String sshKeyPairName;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        super.execute();
        new MakeBaseInfrastructureDelegate().make();
    }


    public String getAvailabilityZones() {
        return availabilityZones;
    }

    public String getSshKeyPairName() {

        return sshKeyPairName;
    }

}
