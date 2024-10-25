package io.github.nahuel92.pit4u.gui.table;

import com.intellij.openapi.util.NlsContexts;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OtherParamsTableModel extends ListTableModel<OtherParamItem<?>> {
    public OtherParamsTableModel(final String otherParams) {
        super(
                new OtherParamsColumnInfo<>("Parameter", OtherParamItem::name),
                new OtherParamsColumnInfo<>("Value", OtherParamItem::value)
        );

        final var advancedArgs = getAdvancedArgs();
        setSavedValues(otherParams, advancedArgs);
        super.setItems(advancedArgs);
    }

    private static void setSavedValues(final String otherParams, final List<OtherParamItem<?>> advancedArgs) {
        Arrays.stream(otherParams.split(System.lineSeparator()))
                .map(e -> e.split(StringUtils.SPACE))
                .forEach(e -> {
                    final var existing = advancedArgs.stream()
                            .filter(e2 -> e2.name().equals(e[0]))
                            .findFirst();
                    existing.ifPresent(otherParamItem -> advancedArgs.set(
                                    advancedArgs.indexOf(otherParamItem),
                                    otherParamItem.from(e[1])
                            )
                    );
                });
    }

    private static List<OtherParamItem<?>> getAdvancedArgs() {
        final var otherParams = List.of(
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
                new OtherParamItem<>("--classPath", StringUtils.EMPTY),
                new OtherParamItem<>("--includedGroups", StringUtils.EMPTY),
                new OtherParamItem<>("--excludedGroups", StringUtils.EMPTY),
                new OtherParamItem<>("--excludedMethods", StringUtils.EMPTY),
                new OtherParamItem<>("--excludedTests", StringUtils.EMPTY),
                new OtherParamItem<>("--historyInputLocation", StringUtils.EMPTY),
                new OtherParamItem<>("--historyOutputLocation", StringUtils.EMPTY),
                new OtherParamItem<>("--jvmArgs", StringUtils.EMPTY),
                new OtherParamItem<>("--jvmPath", StringUtils.EMPTY),
                new OtherParamItem<>("--mutableCodePaths", StringUtils.EMPTY),
                new OtherParamItem<>("--mutators", StringUtils.EMPTY)
        );
        return new ArrayList<>(otherParams);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return getItems().get(rowIndex).name();
        }
        if (columnIndex == 1) {
            return getItems().get(rowIndex).value();
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void setValueAt(final Object aValue, int rowIndex, int columnIndex) {
        final var existingItem = getItems().get(rowIndex);
        setItem(rowIndex, existingItem.from(aValue));
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1;
    }

    public void restoreDefaultValues() {
        super.setItems(getAdvancedArgs());
        fireTableDataChanged();
    }

    public String getUserFriendlyModel() {
        return getModel(StringUtils.SPACE);
    }

    public String getModelToSave() {
        return getModel(System.lineSeparator());
    }

    private String getModel(final String delimiter) {
        return getItems()
                .stream()
                .filter(OtherParamItem::hasBeenUpdated)
                .map(OtherParamItem::toString)
                .collect(Collectors.joining(delimiter));
    }

    private static class OtherParamsColumnInfo<T> extends ColumnInfo<OtherParamItem<?>, T> {
        private final Function<OtherParamItem<?>, T> function;

        OtherParamsColumnInfo(@NlsContexts.ColumnName final String name, final Function<OtherParamItem<?>, T> function) {
            super(name);
            this.function = function;
        }

        @Override
        @Nullable
        public T valueOf(final OtherParamItem<?> otherParamItem) {
            return function.apply(otherParamItem);
        }
    }
}