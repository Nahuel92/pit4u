package io.github.nahuel92.pit4u.configuration;

import org.apache.commons.lang3.StringUtils;
import org.jdom.Attribute;
import org.jdom.Element;

final class PIT4UConfigurationStore {
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
        writeOrRemoveAttribute(element, TARGET_CLASSES, pit4UEditorStatus.getTargetClasses());
        writeOrRemoveAttribute(element, TARGET_TESTS, pit4UEditorStatus.getTargetTests());
        writeOrRemoveAttribute(element, SOURCE_DIRS, pit4UEditorStatus.getSourceDir());
        writeOrRemoveAttribute(element, REPORT_DIR, pit4UEditorStatus.getReportDir());
        writeOrRemoveAttribute(element, OTHER_PARAMS, pit4UEditorStatus.getOtherParams());
    }

    private static String getOrDefault(final Attribute attribute) {
        if (attribute == null) {
            return StringUtils.EMPTY;
        }
        return attribute.getValue();
    }

    private static void writeOrRemoveAttribute(final Element element, final String name, final String value) {
        if (StringUtils.isEmpty(value)) {
            element.removeAttribute(name);
            return;
        }
        element.setAttribute(name, value);
    }
}
