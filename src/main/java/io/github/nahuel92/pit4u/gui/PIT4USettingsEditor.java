package io.github.nahuel92.pit4u.gui;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PackageChooserDialog;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton.BrowseFolderActionListener;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Disposer;
import io.github.nahuel92.pit4u.configuration.PIT4UEditorStatus;
import io.github.nahuel92.pit4u.configuration.PIT4URunConfiguration;
import io.github.nahuel92.pit4u.gui.table.OtherParamsDialog;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.function.Consumer;

public class PIT4USettingsEditor extends SettingsEditor<PIT4URunConfiguration> {
    private static final Logger log = Logger.getInstance(PIT4USettingsEditor.class);
    private final Project project;
    private final PIT4UEditorStatus pit4UEditorStatus;
    private JPanel jPanel;
    private TextFieldWithBrowseButton targetClasses;
    private TextFieldWithBrowseButton targetTests;
    private TextFieldWithBrowseButton sourceDir;
    private TextFieldWithBrowseButton reportDir;
    private OtherParamsButton otherParams;

    public PIT4USettingsEditor(final Project project, final PIT4UEditorStatus pit4UEditorStatus) {
        this.project = project;
        this.pit4UEditorStatus = pit4UEditorStatus;
        this.targetClasses.setText(pit4UEditorStatus.getTargetClasses());
        this.targetTests.setText(pit4UEditorStatus.getTargetTests());
        this.sourceDir.setText(pit4UEditorStatus.getSourceDir());
        this.reportDir.setText(pit4UEditorStatus.getReportDir());
        this.otherParams.setText(pit4UEditorStatus.getOtherParams());
    }

    @Override
    @NotNull
    protected JComponent createEditor() {
        targetClasses.addActionListener(getPackageChooserListener(
                "Select Target Classes Package",
                targetClasses,
                pit4UEditorStatus::setTargetClasses
        ));
        targetTests.addActionListener(getPackageChooserListener(
                "Select Target Tests Package",
                targetTests,
                pit4UEditorStatus::setTargetTests
        ));
        sourceDir.addActionListener(getDirectoryListener(
                "Select Source Directory",
                sourceDir,
                pit4UEditorStatus::setSourceDir
        ));
        reportDir.addActionListener(getDirectoryListener(
                "Select Report Directory",
                reportDir,
                pit4UEditorStatus::setReportDir
        ));
        otherParams.setButtonIcon(AllIcons.FileTypes.Archive);
        otherParams.addActionListener(getOtherParamsDialogListener());
        return jPanel;
    }

    @Override
    protected void resetEditorFrom(@NotNull final PIT4URunConfiguration s) {
    }

    @Override
    protected void applyEditorTo(@NotNull final PIT4URunConfiguration s) {
    }

    @Override
    protected void disposeEditor() {
        super.disposeEditor();

        Arrays.stream(targetClasses.getListeners(ActionListener.class)).forEach(targetClasses::removeActionListener);
        Arrays.stream(targetTests.getListeners(ActionListener.class)).forEach(targetTests::removeActionListener);

        Arrays.stream(sourceDir.getListeners(BrowseFolderActionListener.class))
                .forEach(sourceDir::removeActionListener);

        Arrays.stream(reportDir.getListeners(BrowseFolderActionListener.class))
                .forEach(reportDir::removeActionListener);

        Arrays.stream(otherParams.getListeners(ActionListener.class)).forEach(otherParams::removeActionListener);

        log.info("PIT4USettingsEditor Disposed");
    }

    private ActionListener getPackageChooserListener(final String title, final TextFieldWithBrowseButton field,
                                                     final Consumer<String> editorStatusConsumer) {
        return e -> {
            final var packageChooser = new PackageChooserDialog(title, project);
            final var component = packageChooser.getPreferredFocusedComponent();
            if (component != null) {
                component.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(final KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            getAndSetValue(packageChooser, field, editorStatusConsumer);
                            packageChooser.close(DialogWrapper.OK_EXIT_CODE);
                        }
                    }
                });
            }
            if (packageChooser.showAndGet()) {
                getAndSetValue(packageChooser, field, editorStatusConsumer);
            }
        };
    }

    private void getAndSetValue(final PackageChooserDialog packageChooser, final TextFieldWithBrowseButton field,
                                final Consumer<String> editorStatusConsumer) {
        final var selectedPackage = packageChooser.getSelectedPackage();
        if (selectedPackage != null) {
            field.setText(selectedPackage.getQualifiedName() + ".*");
            editorStatusConsumer.accept(field.getText());
        }
    }

    private ActionListener getDirectoryListener(final String title, final TextFieldWithBrowseButton field,
                                                final Consumer<String> editorStatusConsumer) {
        return e -> {
            new BrowseFolderActionListener<>(
                    field,
                    project,
                    FileChooserDescriptorFactory.createSingleFolderDescriptor()
                            .withTitle(title),
                    TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
            ).actionPerformed(e);
            editorStatusConsumer.accept(field.getText());
        };
    }

    private ActionListener getOtherParamsDialogListener() {
        return e -> ApplicationManager.getApplication().invokeLater(
                () -> {
                    final var otherParamsDialog = new OtherParamsDialog(pit4UEditorStatus.getOtherParams());
                    Disposer.register(this, otherParamsDialog);
                    if (otherParamsDialog.showAndGet()) {
                        otherParams.setText(otherParamsDialog.getUserFriendlyModel());
                        pit4UEditorStatus.setOtherParams(otherParamsDialog.getModelToSave());
                    }
                }
        );
    }
}