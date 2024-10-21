package io.github.nahuel92.pit4u.configuration;

import org.apache.commons.lang3.StringUtils;

public class PIT4UEditorStatus {
    private String targetClasses = StringUtils.EMPTY;
    private String targetTests = StringUtils.EMPTY;
    private String sourceDir = StringUtils.EMPTY;
    private String reportDir = StringUtils.EMPTY;
    private String otherParams = StringUtils.EMPTY;

    public String getTargetClasses() {
        return targetClasses;
    }

    public void setTargetClasses(final String targetClasses) {
        this.targetClasses = targetClasses;
    }

    public String getTargetTests() {
        return targetTests;
    }

    public void setTargetTests(final String targetTests) {
        this.targetTests = targetTests;
    }

    public String getSourceDir() {
        return sourceDir;
    }

    public void setSourceDir(final String sourceDir) {
        this.sourceDir = sourceDir;
    }

    public String getReportDir() {
        return reportDir;
    }

    public void setReportDir(final String reportDir) {
        this.reportDir = reportDir;
    }

    public String getOtherParams() {
        return otherParams;
    }

    public void setOtherParams(final String otherParams) {
        this.otherParams = otherParams;
    }
}
