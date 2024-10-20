package io.github.nahuel92.pit4u.configuration;

import org.apache.commons.lang3.StringUtils;
import org.jdom.Element;

class PIT4UConfigurationStore {
    private static final String TARGET_CLASSES = "targetClasses";
    private static final String TARGET_TESTS = "targetTests";
    private static final String SOURCE_DIRS = "sourceDirs";
    private static final String REPORT_DIR = "reportDir";
    private static final String OTHER_PARAMS = "otherParams";

    public static void readExternal(final PIT4UEditorStatus PIT4UEditorStatus, final Element element) {
        PIT4UEditorStatus.setTargetClasses(element.getAttribute(TARGET_CLASSES).getValue());
        PIT4UEditorStatus.setTargetTests(element.getAttribute(TARGET_TESTS).getValue());
        PIT4UEditorStatus.setSourceDir(element.getAttribute(SOURCE_DIRS).getValue());
        PIT4UEditorStatus.setReportDir(element.getAttribute(REPORT_DIR).getValue());
        PIT4UEditorStatus.setOtherParams(element.getAttribute(OTHER_PARAMS).getValue());
    }

    public static void writeExternal(final PIT4UEditorStatus pit4UEditorStatus, final Element element) {
        element.setAttribute(TARGET_CLASSES, getOrDefault(pit4UEditorStatus.getTargetClasses()));
        element.setAttribute(TARGET_TESTS, getOrDefault(pit4UEditorStatus.getTargetTests()));
        element.setAttribute(SOURCE_DIRS, getOrDefault(pit4UEditorStatus.getSourceDir()));
        element.setAttribute(REPORT_DIR, getOrDefault(pit4UEditorStatus.getReportDir()));
        element.setAttribute(OTHER_PARAMS, getOrDefault(pit4UEditorStatus.getOtherParams()));
    }

    private static String getOrDefault(final String value) {
        return StringUtils.defaultIfBlank(value, StringUtils.EMPTY);
    }
}
