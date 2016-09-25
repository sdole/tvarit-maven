package com.tvarit.plugin;

import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsResult;
import com.amazonaws.services.autoscaling.model.TagDescription;

import java.util.function.Predicate;

class IsThisAnUpdate {


    private AmazonAutoScalingClient amazonAutoScalingClient;

    IsThisAnUpdate(AmazonAutoScalingClient amazonAutoScalingClient) {

        this.amazonAutoScalingClient = amazonAutoScalingClient;
    }

    Deployer find(String projectGroupId, String projectArtifactId, String projectVersion) {
        DescribeAutoScalingGroupsResult describeAutoScalingGroupsResult = amazonAutoScalingClient.describeAutoScalingGroups();
        boolean doesAGroupExist = describeAutoScalingGroupsResult.getAutoScalingGroups().stream().anyMatch(
                autoScalingGroup -> autoScalingGroup.getTags().stream().anyMatch(
                        (Predicate<TagDescription>) tagDescription ->
                                tagDescription.getKey().equals("tvarit:asg:app:mvn:group:artifact:version")
                                        &&
                                        tagDescription.getValue().equals(projectGroupId + ":" + projectArtifactId + ":" + projectVersion)));
        return doesAGroupExist ? new UpdateStackDeployer(null) : new CreateStackDeployer();
    }
}
