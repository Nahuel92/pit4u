package io.github.nahuel92.pit4u.gui.table;

import org.apache.commons.lang3.StringUtils;

public class OtherParamItem<V> {
    protected final V defaultParameterValue;
    private final String parameterName;
    protected V parameterValue;
    private boolean hasBeenUpdated;

    public OtherParamItem(final String parameterName, final V defaultParameterValue) {
        this.parameterName = parameterName;
        this.defaultParameterValue = defaultParameterValue;
        this.parameterValue = defaultParameterValue;
    }

    public String getParameterName() {
        return parameterName;
    }

    public V getParameterValue() {
        return parameterValue;
    }

    @SuppressWarnings("unchecked")
    public void setParameterValue(final Object newValue) {
        this.parameterValue = (V) newValue;
        this.hasBeenUpdated = true;
    }

    public void resetDefaults() {
        this.parameterValue = defaultParameterValue;
    }

    public boolean hasBeenUpdated() {
        return hasBeenUpdated && !defaultParameterValue.equals(parameterValue);
    }

    @Override
    public String toString() {
        return parameterName + StringUtils.SPACE + parameterValue;
    }
}
