package com.wire.bots.don.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Asset {
    @JsonProperty
    public String type = "image";

    @JsonProperty
    public String key;

    @JsonProperty
    public String size;
}
