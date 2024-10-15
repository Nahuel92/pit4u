package io.github.nahuel92.pit4u.gui.table;

import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.Nullable;

import java.util.List;

class MyTableModel extends ListTableModel<MyTableItem<?>> {
    private static final List<MyTableItem<?>> ADVANCED_ARGS = getAdvancedArgs();

    public MyTableModel() {
        super(
                new ColumnInfo[]{new MyTableModel.ParameterColumnInfo(), new MyTableModel.ValueColumnInfo()},
                ADVANCED_ARGS
        );
    }

    private static List<MyTableItem<?>> getAdvancedArgs() {
        return List.of(
                new MyTableItem<>("--detectInlinedCode", true),
                new MyTableItem<>("--includeLaunchClasspath", true),
                new MyTableItem<>("--failWhenNoMutations", true),
                new MyTableItem<>("--timestampedReports", false),
                new MyTableItem<>("--verbose", false),

                new MyTableItem<>("--outputFormats", "HTML"),

                new MyTableItem<>("--timeoutFactor", "1.25"),

                new MyTableItem<>("--timeoutConst", "4000"),
                new MyTableItem<>("--threads", "1"),
                new MyTableItem<>("--coverageThreshold", "0"),
                new MyTableItem<>("--mutationThreshold", "0"),

                new MyTableItem<>("--avoidCallsTo", "java.util.logging,org.apache.log4j,org.slf4j,org.apache.commons.logging"),
                new MyTableItem<>("--classPath", ""),
                new MyTableItem<>("--includedGroups", ""),
                new MyTableItem<>("--excludedGroups", ""),
                new MyTableItem<>("--excludedMethods", ""),
                new MyTableItem<>("--excludedTests", ""),
                new MyTableItem<>("--historyInputLocation", ""),
                new MyTableItem<>("--historyOutputLocation", ""),
                new MyTableItem<>("--jvmArgs", ""),
                new MyTableItem<>("--jvmPath", ""),
                new MyTableItem<>("--mutableCodePaths", ""),
                new MyTableItem<>("--mutators", "")
        );
    }

    @Override
    public int getRowCount() {
        return this.getItems().size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return this.getItems().get(rowIndex).getParameterName();
        }
        if (columnIndex == 1) {
            return this.getItems().get(rowIndex).getParameterValue();
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void setValueAt(final Object aValue, int rowIndex, int columnIndex) {
        final var existingItem = ADVANCED_ARGS.get(rowIndex);
        existingItem.setParameterValue(aValue);
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1;
    }

    public List<MyTableItem<?>> getUpdatedItems() {
        return getItems()
                .stream()
                .filter(MyTableItem::hasBeenUpdated)
                .toList();
    }

    public void restoreDefaultValues() {
        getItems().forEach(MyTableItem::resetDefaults);
        fireTableDataChanged();
    }

    private static class ParameterColumnInfo extends ColumnInfo<MyTableItem<?>, String> {
        public ParameterColumnInfo() {
            super("Parameter");
        }

        @Override
        @Nullable
        public String valueOf(final MyTableItem o) {
            return o.getParameterName();
        }
    }

    private static class ValueColumnInfo extends ColumnInfo<MyTableItem<?>, Object> {
        public ValueColumnInfo() {
            super("Value");
        }

        @Override
        @Nullable
        public Object valueOf(final MyTableItem<?> o) {
            return o.getParameterValue();
        }
    }
}