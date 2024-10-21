package io.github.nahuel92.pit4u.configuration;

import org.apache.commons.lang3.StringUtils;
import org.jdom.Attribute;
import org.jdom.Element;

class PIT4UConfigurationStore {
    private static final String TARGET_CLASSES = "targetClasses";
    private static final String TARGET_TESTS = "targetTests";
    private static final String SOURCE_DIRS = "sourceDirs";
    private static final String REPORT_DIR = "reportDir";
    private static final String OTHER_PARAMS = "otherParams";

    public static void readExternal(final PIT4UEditorStatus pit4UEditorStatus, final Element element) {
        pit4UEditorStatus.setTargetClasses(getOrDefault(element.getAttribute(TARGET_CLASSES)));
        pit4UEditorStatus.setTargetTests(getOrDefault(element.getAttribute(TARGET_TESTS)));
        pit4UEditorStatus.setSourceDir(getOrDefault(element.getAttribute(SOURCE_DIRS)));
        pit4UEditorStatus.setReportDir(getOrDefault(element.getAttribute(REPORT_DIR)));
        pit4UEditorStatus.setOtherParams(getOrDefault(element.getAttribute(OTHER_PARAMS)));
    }

    public static void writeExternal(final PIT4UEditorStatus pit4UEditorStatus, final Element element) {
        element.setAttribute(TARGET_CLASSES, getOrDefault(pit4UEditorStatus.getTargetClasses()));
        element.setAttribute(TARGET_TESTS, getOrDefault(pit4UEditorStatus.getTargetTests()));
        element.setAttribute(SOURCE_DIRS, getOrDefault(pit4UEditorStatus.getSourceDir()));
        element.setAttribute(REPORT_DIR, getOrDefault(pit4UEditorStatus.getReportDir()));
        element.setAttribute(OTHER_PARAMS, getOrDefault(pit4UEditorStatus.getOtherParams()));
    }

    private static String getOrDefault(final Attribute attribute) {
        if (attribute == null) {
            return StringUtils.EMPTY;
        }
        return attribute.getValue();
    }

    private static String getOrDefault(final String value) {
        return StringUtils.defaultIfBlank(value, StringUtils.EMPTY);
    }
}
