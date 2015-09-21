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
        List<String> layersFound = describeLayersResult.getLayers().stream().map(Layer::getName).collect(Collectors.toList());
        if (layersFound.isEmpty() || !layersFound.contains(layerName)) {
            tvaritMojo.getLog().debug("No layers found! Will create!");
            CreateLayerRequest createLayerRequest = new CreateLayerRequest();
            createLayerRequest.withName(layerName.substring(0,31)).withStackId(stackId).withType(LayerType.Custom).withAutoAssignElasticIps(true).withShortname(layerName.substring(0,16));
            CreateLayerResult createLayerResult = awsOpsWorksClient.createLayer(createLayerRequest);
            tvaritMojo.getLog().debug("Created layer! " + createLayerResult.getLayerId());
            return createLayerResult.getLayerId();
        } else {
            tvaritMojo.getLog().debug("Found layer: " + layersFound.toString());
            return layersFound.get(0);
        }
    }
}