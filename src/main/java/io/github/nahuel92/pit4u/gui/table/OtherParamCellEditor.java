package io.github.nahuel92.pit4u.gui.table;

import com.intellij.openapi.ui.ComboBox;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

class OtherParamCellEditor extends AbstractCellEditor implements TableCellEditor {
    private static final String[] OUTPUT_FORMATS = {
            "HTML",
            "XML",
            "CSV",
            "XML,CSV",
            "HTML,XML",
            "HTML,CSV",
            "HTML,XML,CSV"
    };
    private final ComboBox<String> comboBox;
    private final JTextField textField;
    private final JCheckBox checkBox;

    public OtherParamCellEditor() {
        this.comboBox = new ComboBox<>(OUTPUT_FORMATS);
        this.textField = new JTextField();
        this.checkBox = new JCheckBox();
    }

    @Override
    public Object getCellEditorValue() {
        if (checkBox.isShowing()) {
            return checkBox.isSelected();
        }
        if (comboBox.isShowing()) {
            return comboBox.getSelectedItem();
        }
        return textField.getText();
    }

    @Override
    public Component getTableCellEditorComponent(final JTable table, final Object value,
                                                 boolean isSelected, int row, int column) {
        if (row < 5) {
            checkBox.setSelected((Boolean) value);
            return checkBox;
        }
        if (row == 5) {
            comboBox.setSelectedItem(value);
            return comboBox;
        }
        textField.setText(value.toString());
        return textField;
    }
}
