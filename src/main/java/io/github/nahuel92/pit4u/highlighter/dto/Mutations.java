package io.github.nahuel92.pit4u.highlighter.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import java.util.List;

@JsonRootName("mutations")
@JsonIgnoreProperties(ignoreUnknown = true)
public record Mutations(
        @JsonProperty("mutation")
        @JacksonXmlElementWrapper(useWrapping = false)
        @JsonIgnoreProperties(ignoreUnknown = true)
        List<Mutation> mutations) {
}
