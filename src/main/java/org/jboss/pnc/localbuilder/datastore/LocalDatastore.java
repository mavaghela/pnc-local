package org.jboss.pnc.localbuilder.datastore;


import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.Datastore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by aabulawi on 24/06/15.
 */
public class LocalDatastore implements Datastore {

    private List<BuildRecord> buildRecords = Collections.synchronizedList(new ArrayList<BuildRecord>());
    private List<BuildConfigSetRecord> buildConfigSetRecords = Collections.synchronizedList(new ArrayList<BuildConfigSetRecord>());

    AtomicInteger buildRecordSequence = new AtomicInteger(0);

    @Override
    public BuildRecord storeCompletedBuild(BuildRecord buildRecord) {
        buildRecords.add(buildRecord);
        return buildRecord;
    }

    @Override
    public User retrieveUserByUsername(String username) {
        User user = new User();
        user.setUsername("local-user");
        return user;
    }

    @Override
    public void createNewUser(User user) {
    }

    @Override
    public int getNextBuildRecordId() {
        return buildRecordSequence.incrementAndGet();
    }


    //    @Override
    public BuildConfigSetRecord saveBuildConfigSetRecord(BuildConfigSetRecord buildConfigSetRecord){
        buildConfigSetRecords.add(buildConfigSetRecord);
        return buildConfigSetRecord;
    }

    //    @Override
    // TODO: to implement
    public BuildRecord storeBuildRecord(BuildRecord buildRecord, List<ProductMilestone> milestones) {
        return null;
    }

    // TODO: to implement
    public int getNextBuildConfigSetRecordId() {return 0;}
}
