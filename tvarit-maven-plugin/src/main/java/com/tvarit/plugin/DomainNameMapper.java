package com.tvarit.plugin;

import com.amazonaws.services.route53.AmazonRoute53Client;
import com.amazonaws.services.route53.model.*;

/**
 * Created by sachi_000 on 1/24/2016.
 */
public class DomainNameMapper {
    public void map(AmazonRoute53Client amazonRoute53Client, String publicIp) {
        final ChangeResourceRecordSetsRequest changeResourceRecordSetsRequest = new ChangeResourceRecordSetsRequest();
        final ChangeBatch changeBatch = new ChangeBatch();
        changeBatch.withComment("a comment");
        final Change change = new Change();
        change.withAction(ChangeAction.UPSERT);
        final ResourceRecordSet resourceRecordSet = new ResourceRecordSet();
        final ResourceRecord resourceRecord = new ResourceRecord();
        resourceRecord.withValue(publicIp);
        resourceRecordSet.withName("auto.tvarit.io").withType(RRType.A).withResourceRecords(resourceRecord).withTTL(300l);
        change.withResourceRecordSet(resourceRecordSet);
        changeBatch.withChanges(change);
        changeResourceRecordSetsRequest.withChangeBatch(changeBatch).withHostedZoneId("Z1PILE99ZB6S2A");
        amazonRoute53Client.changeResourceRecordSets(changeResourceRecordSetsRequest);
    }
}
