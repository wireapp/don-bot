package com.wire.bots.don.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PubKey {
    @JsonProperty
    public String pem;
    @JsonProperty
    public String type;
    @JsonProperty
    public int size;

}
