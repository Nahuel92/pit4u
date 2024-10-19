package io.github.nahuel92.pit4u.gui;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PackageChooserDialog;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Disposer;
import io.github.nahuel92.pit4u.configuration.Pit4URunConfiguration;
import io.github.nahuel92.pit4u.gui.table.OtherParamItem;
import io.github.nahuel92.pit4u.gui.table.OtherParamsDialog;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.stream.Collectors;

public class Pit4USettingsEditor extends SettingsEditor<Pit4URunConfiguration> {
    private static final Logger log = Logger.getInstance(Pit4USettingsEditor.class);
    private JPanel jPanel;
    private TextFieldWithBrowseButton targetClasses;
    private final ActionListener targetClassesActionListener;
    private TextFieldWithBrowseButton targetTests;
    private final ActionListener targetTestsActionListener;
    private TextFieldWithBrowseButton sourceDir;
    private TextFieldWithBrowseButton reportDir;
    private final ActionListener otherParamsActionListener;
    private TextFieldWithBrowseButton otherParams;

    public Pit4USettingsEditor(final Project project) {
        Disposer.register(project, this);

        this.targetClassesActionListener = getActionListener(
                "Select Target Classes Package",
                project,
                this.targetClasses
        );
        this.targetClasses.addActionListener(this.targetClassesActionListener);

        this.targetTestsActionListener = getActionListener(
                "Select Target Tests Package",
                project,
                this.targetTests
        );
        this.targetTests.addActionListener(this.targetTestsActionListener);

        this.sourceDir.addBrowseFolderListener(
                "Select Source Directory",
                null,
                project,
                FileChooserDescriptorFactory.createSingleFolderDescriptor()
        );

        this.reportDir.addBrowseFolderListener(
                "Select Report Directory",
                null,
                project,
                FileChooserDescriptorFactory.createSingleFolderDescriptor()
        );

        this.otherParams.setButtonIcon(AllIcons.FileTypes.Archive);
        this.otherParamsActionListener = getActionListener();
        this.otherParams.addActionListener(this.otherParamsActionListener);
    }

    @Override
    protected void disposeEditor() {
        log.info("Disposing Settings Editor");
        targetClasses.removeActionListener(this.targetClassesActionListener);
        targetTests.removeActionListener(this.targetTestsActionListener);

        for (final var actionListener : sourceDir.getListeners(ComponentWithBrowseButton.BrowseFolderActionListener.class)) {
            sourceDir.removeActionListener(actionListener);
        }

        for (final var actionListener : reportDir.getListeners(ComponentWithBrowseButton.BrowseFolderActionListener.class)) {
            reportDir.removeActionListener(actionListener);
        }
        otherParams.removeActionListener(otherParamsActionListener);
        log.info("Settings Editor disposed");
    }

    @Override
    @NotNull
    protected JComponent createEditor() {
        return jPanel;
    }

    @Override
    protected void resetEditorFrom(@NotNull final Pit4URunConfiguration s) {
    }

    @Override
    protected void applyEditorTo(@NotNull final Pit4URunConfiguration s) {
    }

    public String getTargetClasses() {
        return this.targetClasses.getText();
    }

    public void setTargetClasses(final String targetClasses) {
        this.targetClasses.setText(targetClasses);
    }

    public String getTargetTests() {
        return this.targetTests.getText();
    }

    public void setTargetTests(final String targetTests) {
        this.targetTests.setText(targetTests);
    }

    public String getSourceDirs() {
        return this.sourceDir.getText();
    }

    public void setSourceDirs(final String sourceDirs) {
        this.sourceDir.setText(sourceDirs);
    }

    public String getReportDir() {
        return this.reportDir.getText();
    }

    public void setReportDir(final String reportDir) {
        this.reportDir.setText(reportDir);
    }

    private ActionListener getActionListener(final String title, final Project project,
                                             final TextFieldWithBrowseButton field) {
        return e -> {
            final var packageChooser = new PackageChooserDialog(title, project);
            if (packageChooser.showAndGet()) {
                field.setText(packageChooser.getSelectedPackage().getQualifiedName() + ".*");
            }
        };
    }

    private ActionListener getActionListener() {
        return e -> ApplicationManager.getApplication().invokeLater(
                () -> {
                    final var otherParamsDialog = new OtherParamsDialog();
                    if (otherParamsDialog.showAndGet()) {
                        final var otherParameters = otherParamsDialog.getTableItems()
                                .stream()
                                .map(OtherParamItem::toString)
                                .collect(Collectors.joining(" "));
                        otherParams.setText(otherParameters);
                    }
                }
        );
    }
}