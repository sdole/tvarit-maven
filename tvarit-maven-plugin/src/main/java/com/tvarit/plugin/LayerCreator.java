package com.tvarit.plugin;

import com.amazonaws.services.opsworks.AWSOpsWorksClient;
import com.amazonaws.services.opsworks.model.*;

import java.util.List;
import java.util.stream.Collectors;

public class LayerCreator {
    public String create(AWSOpsWorksClient awsOpsWorksClient, String layerName, TvaritMojo tvaritMojo, String stackId, String securityGroupId) {
        DescribeLayersRequest describeLayersRequest = new DescribeLayersRequest();
        describeLayersRequest.withStackId(stackId);
        DescribeLayersResult describeLayersResult = awsOpsWorksClient.describeLayers(describeLayersRequest);
        final List<Layer> layers = describeLayersResult.getLayers();
        List<String> layersFound = layers.stream().map(Layer::getName).collect(Collectors.toList());
        if (layersFound.isEmpty() || !layersFound.contains(layerName)) {
            tvaritMojo.getLog().debug("No layers found! Will create!");
            CreateLayerRequest createLayerRequest = new CreateLayerRequest();
            final Recipes customRecipes = new Recipes();
            customRecipes.withSetup("tvarit-cookbook::default");
            createLayerRequest.withCustomRecipes(customRecipes).
                    withName(layerName).
                    withStackId(stackId).
                    withType(LayerType.Custom).
                    withAutoAssignElasticIps(true).
                    withShortname(layerName).
                    withCustomSecurityGroupIds(securityGroupId)
                    ;
            CreateLayerResult createLayerResult = awsOpsWorksClient.createLayer(createLayerRequest);
            tvaritMojo.getLog().debug("Created layer! " + createLayerResult.getLayerId());
            return createLayerResult.getLayerId();
        } else {
            tvaritMojo.getLog().debug("Found layer: " + layersFound.toString());
            return layers.get(0).getLayerId();
        }
    }
}
