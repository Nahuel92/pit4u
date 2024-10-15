package io.github.nahuel92.pit4u.gui;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PackageChooserDialog;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import io.github.nahuel92.pit4u.configuration.Pit4URunConfiguration;
import io.github.nahuel92.pit4u.gui.table.MyDialog;
import io.github.nahuel92.pit4u.gui.table.MyTableItem;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.stream.Collectors;

public class Pit4USettingsEditor extends SettingsEditor<Pit4URunConfiguration> {
    private JPanel jPanel;
    private TextFieldWithBrowseButton targetClasses;
    private TextFieldWithBrowseButton targetTests;
    private TextFieldWithBrowseButton sourceDir;
    private TextFieldWithBrowseButton reportDir;
    private TextFieldWithBrowseButton otherParams;

    public Pit4USettingsEditor(final Project project) {
        addListener(this.targetClasses, "Select Target Classes Package", project);
        addListener(this.targetTests, "Select Target Tests Package", project);
        addListener(this.sourceDir, "Select Source Directory");
        addListener(this.reportDir, "Select Report Directory");
        this.otherParams.setButtonIcon(AllIcons.FileTypes.Archive);
        this.otherParams.addActionListener(addListener());
    }

    public String getTargetClasses() {
        return targetClasses.getText();
    }

    public void setTargetClasses(final String targetClasses) {
        this.targetClasses.setText(targetClasses);
    }

    public String getTargetTests() {
        return targetTests.getText();
    }

    public void setTargetTests(final String targetTests) {
        this.targetTests.setText(targetTests);
    }

    public String getSourceDirs() {
        return sourceDir.getText();
    }

    public void setSourceDirs(final String sourceDirs) {
        this.sourceDir.setText(sourceDirs);
    }

    public String getReportDir() {
        return reportDir.getText();
    }

    public void setReportDir(final String reportDir) {
        this.reportDir.setText(reportDir);
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

    private void addListener(final TextFieldWithBrowseButton field, final String title, final Project project) {
        field.addActionListener(e -> {
            final var packageChooser = new PackageChooserDialog(title, project);
            if (packageChooser.showAndGet()) {
                field.setText(packageChooser.getSelectedPackage().getQualifiedName() + ".*");
            }
        });
    }

    private void addListener(final TextFieldWithBrowseButton field, final String title) {
        field.addBrowseFolderListener(
                title,
                null,
                null,
                FileChooserDescriptorFactory.createSingleFolderDescriptor()
        );
    }

    private ActionListener addListener() {
        return e -> ApplicationManager.getApplication().invokeLater(
                () -> {
                    final var myDialog = new MyDialog();
                    if (myDialog.showAndGet()) {
                        final var otherParams1 = myDialog.getTableItems()
                                .stream()
                                .map(MyTableItem::toString)
                                .collect(Collectors.joining(" "));
                        this.otherParams.setText(otherParams1);
                    }
                }
        );
    }
}