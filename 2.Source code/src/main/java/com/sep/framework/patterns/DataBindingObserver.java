package com.sep.framework.patterns;

/**
 * Observer Pattern: Interface cho data binding observers
 */
public interface DataBindingObserver {
    void onDataChanged(Object source, String property, Object oldValue, Object newValue);
}

