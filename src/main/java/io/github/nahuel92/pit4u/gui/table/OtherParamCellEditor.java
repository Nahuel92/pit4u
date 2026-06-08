package io.github.nahuel92.pit4u.gui.table;

import com.intellij.openapi.ui.ComboBox;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import java.awt.Component;

final class OtherParamCellEditor extends AbstractCellEditor implements TableCellEditor {
    private static final String[] OUTPUT_FORMATS = {
            "XML",
            "XML,CSV",
            "HTML,XML",
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
        return switch (value) {
            case Boolean b -> {
                checkBox.setSelected(b);
                yield checkBox;
            }
            case String s when row == 5 -> {
                comboBox.setSelectedItem(s);
                yield comboBox;
            }
            default -> {
                textField.setText(value.toString());
                yield textField;
            }
        };
    }
}
