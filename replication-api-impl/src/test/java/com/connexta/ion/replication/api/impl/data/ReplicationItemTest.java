package com.connexta.ion.replication.api.impl.data;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.connexta.ion.replication.api.ReplicationItem;
import com.jparams.verifier.tostring.NameStyle;
import com.jparams.verifier.tostring.ToStringVerifier;
import org.junit.Test;

public class ReplicationItemTest {

  private static final String ID = "id";

  private static final String CONFIG_ID = "configId";

  private static final String SOURCE = "source";

  private static final String DESTINATION = "destination";

  private ReplicationItem replicationItem;

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderIdRequired() {
    new ReplicationItemImpl.Builder("", CONFIG_ID, SOURCE, DESTINATION).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderConfigIdRequired() {
    new ReplicationItemImpl.Builder(ID, "", SOURCE, DESTINATION).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderSourceRequired() {
    new ReplicationItemImpl.Builder(ID, CONFIG_ID, "", DESTINATION).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderDestinationRequired() {
    new ReplicationItemImpl.Builder(ID, CONFIG_ID, SOURCE, "").build();
  }

  @Test
  public void testResourceTransferRate() {
    replicationItem = new TestReplicationItem();
    assertThat(replicationItem.getResourceTransferRate(), is(.001));
  }

  @Test
  public void testResourceTransferRateWithoutDuration() {
    replicationItem =
        new ReplicationItemImpl.Builder(ID, CONFIG_ID, SOURCE, DESTINATION)
            .resourceSize(10)
            .build();
    assertThat(replicationItem.getResourceTransferRate(), is(0.0D));
  }

  @Test
  public void testResourceTransferRateWithoutSize() {
    //    replicationItem = new ReplicationItemImpl.Builder(ID, CONFIG_ID, SOURCE,
    // DESTINATION).build();
    //    replicationItem.markStartTime();
    //    replicationItem.markDoneTime();
    //    assertThat(replicationItem.getResourceTransferRate(), is(0.0D));
    assert false;
  }

  @Test
  public void testToString() {
    ToStringVerifier.forClass(ReplicationItemImpl.class)
        .withClassName(NameStyle.SIMPLE_NAME)
        .verify();
  }

  class TestReplicationItem extends ReplicationItemImpl {

    @Override
    public long getDuration() {
      return 5;
    }

    @Override
    public long getResourceSize() {
      return 5;
    }
  }
}
