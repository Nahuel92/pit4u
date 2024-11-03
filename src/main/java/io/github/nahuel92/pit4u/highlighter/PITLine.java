package io.github.nahuel92.pit4u.highlighter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("mutations")
@JsonIgnoreProperties(ignoreUnknown = true)
public record PITLine(
        @JsonProperty("detected") boolean detected,
        @JsonProperty("status") Status status,
        @JsonProperty("numberOfTestsRun") int numberOfTestsRun,
        @JsonProperty("mutatedClass") String mutatedClass,
        @JsonProperty("lineNumber") int lineNumber) {
    enum Status {
        NO_COVERAGE,
        KILLED
    }
}