package com.tvarit.plugin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.entity.StringEntity;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by sachi_000 on 3/11/14.
 */
public class DeployRequestMaker {

    public StringEntity make(ObjectNode treeNode, String name) throws JsonProcessingException, UnsupportedEncodingException {

        DeployJson deployJson = new DeployJson();
        ArrayList<Address> addresses = new ArrayList<>();
        Address address = new Address();
        address.setDeployment(name);
//        address.setServerGroup(Arrays.asList(new String[]{"trelair-server-group"}));
//        address.setHost("master");
        addresses.add(address);
        deployJson.setAddress(addresses);
        ArrayList<Hash> content = new ArrayList<>();
        Hash hash = new Hash();
        BytesValue bytesValue = new BytesValue();
        bytesValue.setBytesValue(((ObjectNode) treeNode.get("result")).get("BYTES_VALUE").textValue());
        hash.setHash(bytesValue);
        content.add(hash);
        deployJson.setContent(content);
        String string = new ObjectMapper().writeValueAsString(deployJson);
        StringEntity entity = new StringEntity(string);
        entity.setContentType("application/json");
        return entity;
    }
}
