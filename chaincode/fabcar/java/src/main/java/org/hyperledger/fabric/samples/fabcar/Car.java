package org.hyperledger.fabric.samples.fabcar;

import com.owlike.genson.annotation.JsonProperty;

public class Car {
    final String make;
    final String model;
    final String color;
    final String owner;

    public Car(@JsonProperty("make") final String make, @JsonProperty("model") final String model,
            @JsonProperty("color") final String color, @JsonProperty("owner") final String owner) {
        this.make = make;
        this.model = model;
        this.color = color;
        this.owner = owner;
    }
}
