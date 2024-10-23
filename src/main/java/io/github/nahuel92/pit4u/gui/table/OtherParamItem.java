package io.github.nahuel92.pit4u.gui.table;

import org.apache.commons.lang3.StringUtils;

public record OtherParamItem<V>(String name, V defaultValue, V value) {
    public OtherParamItem(String name, V defaultValue) {
        this(name, defaultValue, defaultValue);
    }

    public OtherParamItem<V> from(final V newValue) {
        return new OtherParamItem<>(name, defaultValue, newValue);
    }

    public OtherParamItem<V> resetDefaults() {
        return new OtherParamItem<>(name, defaultValue);
    }

    public boolean hasBeenUpdated() {
        return !defaultValue.equals(value);
    }

    @Override
    public String toString() {
        return name + StringUtils.SPACE + value;
    }
}
