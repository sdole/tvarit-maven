package com.tvarit.plugin;

import org.apache.maven.project.MavenProject;

class UpdateStackDeployer implements Deployer {
    MavenProject project;

    UpdateStackDeployer(MavenProject project) {
        super();
        this.project = project;
    }

    @Override
    public void perform() {
        new TempStackMaker().makeTempStack(project);
    }
}
