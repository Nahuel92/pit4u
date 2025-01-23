package io.github.nahuel92.pit4u.highlighter;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Mutation(
        @JsonProperty("detected") boolean detected,
        @JsonProperty("status") Status status,
        @JsonProperty("numberOfTestsRun") int numberOfTestsRun,
        @JsonProperty("sourceFile") String sourceFile,
        @JsonProperty("mutatedClass") String mutatedClass,
        @JsonProperty("mutatedMethod") String mutatedMethod,
        @JsonProperty("lineNumber") int lineNumber,
        @JsonProperty("description") String description) {
    public enum Status {
        @JsonProperty("NO_COVERAGE") NO_COVERAGE,
        @JsonProperty("KILLED") KILLED
    }
}