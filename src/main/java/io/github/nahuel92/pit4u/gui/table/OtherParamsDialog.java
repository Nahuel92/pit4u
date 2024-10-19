package io.github.nahuel92.pit4u.gui.table;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.table.TableView;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class OtherParamsDialog extends DialogWrapper {
    private static final Logger log = Logger.getInstance(OtherParamsDialog.class);
    private final OtherParamsTableModel otherParamsTableModel;
    private final TableView<OtherParamItem<?>> table;
    private final JButton defaultButton;
    private final ActionListener defaultButtonActionListener;

    public OtherParamsDialog() {
        super(true);

        this.otherParamsTableModel = new OtherParamsTableModel();
        this.table = new TableView<>(this.otherParamsTableModel);
        this.table.getColumnModel()
                .getColumn(1)
                .setCellEditor(new MyCellEditor());

        this.defaultButton = new JButton("Reset Defaults");
        this.defaultButtonActionListener = e -> this.otherParamsTableModel.restoreDefaultValues();
        defaultButton.addActionListener(this.defaultButtonActionListener);

        setTitle("Pit4U - Other Parameters");
        setSize(700, 600);
        init();
    }

    @Override
    @Nullable
    protected JComponent createCenterPanel() {
        final var panel = new JPanel(new BorderLayout());
        panel.add(table, BorderLayout.CENTER);
        final var buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(defaultButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    @Override
    public void dispose() {
        super.dispose();
        log.info("Disposing other parameters dialog");
        defaultButton.removeActionListener(defaultButtonActionListener);
        log.info("Other parameters dialog disposed");
    }

    public List<OtherParamItem<?>> getTableItems() {
        return otherParamsTableModel.getUpdatedItems();
    }
}