package io.github.nahuel92.pit4u.gui.table;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.table.TableView;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MyDialog extends DialogWrapper {
    private final MyTableModel myTableModel;
    private final TableView<MyTableItem<?>> table;

    public MyDialog() {
        super(true);
        this.myTableModel = new MyTableModel();
        this.table = new TableView<>(myTableModel);
        this.table.getColumnModel()
                .getColumn(1)
                .setCellEditor(new MyCellEditor());
        setTitle("Pit4U - Advanced Configuration");
        setSize(700, 600);
        init();
    }

    @Override
    @Nullable
    protected JComponent createCenterPanel() {
        final var defaultButton = new JButton("Reset Defaults");
        defaultButton.addActionListener(e -> myTableModel.restoreDefaultValues());

        final var buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(defaultButton);

        final var panel = new JPanel(new BorderLayout());
        panel.add(table, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    public List<MyTableItem<?>> getTableItems() {
        return myTableModel.getUpdatedItems();
    }
}