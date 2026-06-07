package io.github.nahuel92.pit4u.gui.table;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public record OtherParamItem<V>(String name, V defaultValue, V value) {
    public OtherParamItem(String name, V defaultValue) {
        this(name, defaultValue, defaultValue);
    }

    public OtherParamItem<V> from(final Object newValue) {
        final var typedNewValue = switch (newValue) {
            case Boolean b -> b;
            case String s -> s;
            case Float f -> f;
            case Integer i -> i;
            default -> throw new IllegalStateException("Unexpected value: " + value);
        };
        return new OtherParamItem<>(name, defaultValue, (V) typedNewValue);
    }

    public boolean hasBeenUpdated() {
        return !defaultValue.equals(value);
    }

    @Override
    @NotNull
    public String toString() {
        return name + StringUtils.SPACE + value;
    }
}
