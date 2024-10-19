package io.github.nahuel92.pit4u.gui.table;

import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.Nullable;

import java.util.List;

class OtherParamsTableModel extends ListTableModel<OtherParamItem<?>> {
    private static final List<OtherParamItem<?>> ADVANCED_ARGS = getAdvancedArgs();

    public OtherParamsTableModel() {
        super(
                new ColumnInfo[]{new OtherParamsTableModel.ParameterColumnInfo(), new OtherParamsTableModel.ValueColumnInfo()},
                ADVANCED_ARGS
        );
    }

    private static List<OtherParamItem<?>> getAdvancedArgs() {
        return List.of(
                new OtherParamItem<>("--detectInlinedCode", true),
                new OtherParamItem<>("--includeLaunchClasspath", true),
                new OtherParamItem<>("--failWhenNoMutations", true),
                new OtherParamItem<>("--timestampedReports", false),
                new OtherParamItem<>("--verbose", false),

                new OtherParamItem<>("--outputFormats", "HTML"),

                new OtherParamItem<>("--timeoutFactor", "1.25"),

                new OtherParamItem<>("--timeoutConst", "4000"),
                new OtherParamItem<>("--threads", "1"),
                new OtherParamItem<>("--coverageThreshold", "0"),
                new OtherParamItem<>("--mutationThreshold", "0"),

                new OtherParamItem<>("--avoidCallsTo", "java.util.logging,org.apache.log4j,org.slf4j,org.apache.commons.logging"),
                new OtherParamItem<>("--classPath", ""),
                new OtherParamItem<>("--includedGroups", ""),
                new OtherParamItem<>("--excludedGroups", ""),
                new OtherParamItem<>("--excludedMethods", ""),
                new OtherParamItem<>("--excludedTests", ""),
                new OtherParamItem<>("--historyInputLocation", ""),
                new OtherParamItem<>("--historyOutputLocation", ""),
                new OtherParamItem<>("--jvmArgs", ""),
                new OtherParamItem<>("--jvmPath", ""),
                new OtherParamItem<>("--mutableCodePaths", ""),
                new OtherParamItem<>("--mutators", "")
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

    public List<OtherParamItem<?>> getUpdatedItems() {
        return getItems()
                .stream()
                .filter(OtherParamItem::hasBeenUpdated)
                .toList();
    }

    public void restoreDefaultValues() {
        getItems().forEach(OtherParamItem::resetDefaults);
        fireTableDataChanged();
    }

    private static class ParameterColumnInfo extends ColumnInfo<OtherParamItem<?>, String> {
        public ParameterColumnInfo() {
            super("Parameter");
        }

        @Override
        @Nullable
        public String valueOf(final OtherParamItem o) {
            return o.getParameterName();
        }
    }

    private static class ValueColumnInfo extends ColumnInfo<OtherParamItem<?>, Object> {
        public ValueColumnInfo() {
            super("Value");
        }

        @Override
        @Nullable
        public Object valueOf(final OtherParamItem<?> o) {
            return o.getParameterValue();
        }
    }
}