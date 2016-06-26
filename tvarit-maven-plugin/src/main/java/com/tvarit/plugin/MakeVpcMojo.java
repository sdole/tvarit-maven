package com.tvarit.plugin;

import com.tvarit.plugin.vpc.MakeVpcDelegate;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "make-vpc")
public class MakeVpcMojo extends AbstractTvaritMojo {

    @Parameter(required = true,name = "availability-zones")
    private String availabilityZones;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        super.execute();
        new MakeVpcDelegate().make();
    }


    public String getAvailabilityZones() {
        return availabilityZones;
    }
}
