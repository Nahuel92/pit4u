package io.github.nahuel92.pit4u.gui.table;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.table.TableView;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Arrays;

public class OtherParamsDialog extends DialogWrapper implements Disposable {
    private static final Logger LOGGER = Logger.getInstance(OtherParamsDialog.class);
    private final OtherParamsTableModel otherParamsTableModel;
    private final TableView<OtherParamItem<?>> table;
    private final JButton defaultButton;

    public OtherParamsDialog(final String otherParams) {
        super(true);

        this.otherParamsTableModel = new OtherParamsTableModel(otherParams);
        this.table = new TableView<>(this.otherParamsTableModel);
        this.table.getColumnModel()
                .getColumn(1)
                .setCellEditor(new OtherParamCellEditor());
        this.defaultButton = new JButton("Reset Defaults");

        setTitle("Pit4U - Other Parameters");
        final var screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width - 400, screenSize.height - 400);
        init();
    }

    @Override
    @Nullable
    protected JComponent createCenterPanel() {
        defaultButton.addActionListener(e -> otherParamsTableModel.restoreDefaultValues());
        return getCenterPanel();
    }

    @Override
    public void dispose() {
        super.dispose();
        Arrays.stream(defaultButton.getListeners(ActionListener.class)).forEach(defaultButton::removeActionListener);
        LOGGER.info("Other Parameters Dialog Disposed");
    }

    public String getUserFriendlyModel() {
        return otherParamsTableModel.getUserFriendlyModel();
    }

    public String getModelToSave() {
        return otherParamsTableModel.getModelToSave();
    }

    private JPanel getCenterPanel() {
        final var panel = new JPanel(new BorderLayout());
        panel.add(table, BorderLayout.CENTER);
        panel.add(getButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel getButtonPanel() {
        final var buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(defaultButton);
        return buttonPanel;
    }
}