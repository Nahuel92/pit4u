package io.github.nahuel92.pit4u.configuration;

import io.github.nahuel92.pit4u.gui.Pit4USettingsEditor;
import org.jdom.Element;

class Pit4UConfigurationStore {
    private static final String TARGET_CLASSES = "targetClasses";
    private static final String TARGET_TESTS = "targetTests";
    private static final String SOURCE_DIRS = "sourceDirs";
    private static final String REPORT_DIR = "reportDir";

    public static void readExternal(final Pit4USettingsEditor pit4USettingsEditor, final Element element) {
        pit4USettingsEditor.setTargetClasses(element.getAttribute(TARGET_CLASSES).getValue());
        pit4USettingsEditor.setTargetTests(element.getAttribute(TARGET_TESTS).getValue());
        pit4USettingsEditor.setSourceDirs(element.getAttribute(SOURCE_DIRS).getValue());
        pit4USettingsEditor.setReportDir(element.getAttribute(REPORT_DIR).getValue());
    }

    public static void writeExternal(final Pit4USettingsEditor pit4USettingsEditor, final Element element) {
        element.setAttribute(TARGET_CLASSES, pit4USettingsEditor.getTargetClasses());
        element.setAttribute(TARGET_TESTS, pit4USettingsEditor.getTargetTests());
        element.setAttribute(SOURCE_DIRS, pit4USettingsEditor.getSourceDirs());
        element.setAttribute(REPORT_DIR, pit4USettingsEditor.getReportDir());
    }
}
