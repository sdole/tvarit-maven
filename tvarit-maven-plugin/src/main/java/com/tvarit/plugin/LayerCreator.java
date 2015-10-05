package com.tvarit.plugin;

import com.amazonaws.services.opsworks.AWSOpsWorksClient;
import com.amazonaws.services.opsworks.model.*;

import java.util.List;
import java.util.stream.Collectors;

public class LayerCreator {
    public String create(AWSOpsWorksClient awsOpsWorksClient, String layerName, TvaritMojo tvaritMojo, String stackId) {
        DescribeLayersRequest describeLayersRequest = new DescribeLayersRequest();
        describeLayersRequest.withStackId(stackId);
        DescribeLayersResult describeLayersResult = awsOpsWorksClient.describeLayers(describeLayersRequest);
        final List<Layer> layers = describeLayersResult.getLayers();
        List<String> layersFound = layers.stream().map(Layer::getName).collect(Collectors.toList());
        final String shortenedLayerName = layerName.substring(0, 31);
        if (layersFound.isEmpty() || !layersFound.contains(shortenedLayerName)) {
            tvaritMojo.getLog().debug("No layers found! Will create!");
            CreateLayerRequest createLayerRequest = new CreateLayerRequest();
            final Recipes customRecipes = new Recipes();
            customRecipes.withSetup("tvarit-cookbook::default");
            createLayerRequest.withCustomRecipes(customRecipes).
                    withName(shortenedLayerName).
                    withStackId(stackId).
                    withType(LayerType.Custom).
                    withAutoAssignElasticIps(true).
                    withShortname(layerName.substring(0,16));
            CreateLayerResult createLayerResult = awsOpsWorksClient.createLayer(createLayerRequest);
            tvaritMojo.getLog().debug("Created layer! " + createLayerResult.getLayerId());
            return createLayerResult.getLayerId();
        } else {
            tvaritMojo.getLog().debug("Found layer: " + layersFound.toString());
            return layers.get(0).getLayerId();
        }
    }
}
