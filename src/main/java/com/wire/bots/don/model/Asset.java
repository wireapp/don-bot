package com.wire.bots.don.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
* Created with IntelliJ IDEA.
* User: dejankovacevic
* Date: 29/12/16
* Time: 23:09
*/
public class Asset {
    @JsonProperty
    public String type = "image";

    @JsonProperty
    public String key;

    @JsonProperty
    public String size;
}
