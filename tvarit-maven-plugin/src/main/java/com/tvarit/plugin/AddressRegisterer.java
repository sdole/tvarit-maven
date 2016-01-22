package com.tvarit.plugin;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.Address;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.opsworks.AWSOpsWorksClient;
import com.amazonaws.services.opsworks.model.RegisterElasticIpRequest;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by sachi_000 on 1/21/2016.
 */
public class AddressRegisterer {
    public String registerIpAddress(AmazonEC2Client amazonEC2Client, AWSOpsWorksClient awsOpsWorksClient,String stackId) {
        final DescribeAddressesResult describeAddressesResult = amazonEC2Client.describeAddresses();
        final List<Address> addresses = describeAddressesResult.getAddresses();
        final List<Address> addressStream =  addresses.stream().filter(address -> address.getAssociationId() == null).collect(Collectors.toList());
        final int numAddressesAvailable = addressStream.size();
        String publicIp = null;
        if (numAddressesAvailable == 0) {
            if (addresses.size() >= 5) {
                throw new RuntimeException("AWS max Public IP address limit has been reached. Cannot continue");
            }
            final AllocateAddressRequest allocateAddressRequest = new AllocateAddressRequest();
            allocateAddressRequest.withDomain(DomainType.Vpc);
            final AllocateAddressResult allocateAddressResult = amazonEC2Client.allocateAddress(allocateAddressRequest);
            publicIp = allocateAddressResult.getPublicIp();
        } else {
            publicIp = addressStream.get(0).getPublicIp();
        }
        RegisterElasticIpRequest registerElasticIpRequest = new RegisterElasticIpRequest();
        registerElasticIpRequest.withElasticIp(publicIp).withStackId(stackId);
        awsOpsWorksClient.registerElasticIp(registerElasticIpRequest);
        return publicIp;
    }
}
