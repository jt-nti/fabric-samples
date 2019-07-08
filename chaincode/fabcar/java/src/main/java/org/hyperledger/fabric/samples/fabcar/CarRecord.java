package org.hyperledger.fabric.samples.fabcar;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType()
public final class CarRecord {

    @Property()
    private final String key;

    @Property()
    private final Car record;

    public CarRecord(final String key, final Car record) {
      this.key = key;
      this.record = record;
    }

    public String getKey() {
        return key;
    }

    public Car getRecord() {
        return record;
    }
}
