package com.tvarit.plugin;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by sachind on 10/5/2015.
 */
public class InternetGatewayCreator {
    public String create(AmazonEC2Client amazonEc2Client, String vpcId) {
        final CreateInternetGatewayResult internetGateway = amazonEc2Client.createInternetGateway();
        final AttachInternetGatewayRequest attachInternetGatewayRequest = new AttachInternetGatewayRequest();
        final String internetGatewayId = internetGateway.getInternetGateway().getInternetGatewayId();
        attachInternetGatewayRequest.withInternetGatewayId(internetGatewayId).withVpcId(vpcId);
        amazonEc2Client.attachInternetGateway(attachInternetGatewayRequest);
        final List<RouteTable> myRouteTables = amazonEc2Client.describeRouteTables().getRouteTables().stream().filter(new Predicate<RouteTable>() {
            @Override
            public boolean test(RouteTable routeTable) {
                return routeTable.getVpcId().equals(vpcId);
            }
        }).collect(Collectors.toList());
        final RouteTable routeTable = myRouteTables.get(0);
        final CreateRouteRequest createRouteRequest = new CreateRouteRequest();
        createRouteRequest.withGatewayId(internetGatewayId).withRouteTableId(routeTable.getRouteTableId()).withDestinationCidrBlock("0.0.0.0/0");
        final CreateRouteResult route = amazonEc2Client.createRoute(createRouteRequest);
        return internetGatewayId;
    }
}
