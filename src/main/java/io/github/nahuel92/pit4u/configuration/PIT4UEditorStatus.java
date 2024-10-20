package io.github.nahuel92.pit4u.configuration;

public class PIT4UEditorStatus {
    private String targetClasses;
    private String targetTests;
    private String sourceDir;
    private String reportDir;
    private String otherParams;

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
