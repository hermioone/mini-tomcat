package org.hermione.minit.core;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ContainerListenerDef {

    @Getter
    @Setter
    private String description = null;

    @Getter
    @Setter
    private String displayName = null;

    @Getter
    @Setter
    private String listenerClass = null;

    @Getter
    @Setter
    private String listenerName = null;

    @Getter
    private final Map<String, String> parameters = new ConcurrentHashMap<>();

    public void addInitParameter(String name, String value) {
        parameters.put(name, value);
    }

    public String toString() {
        return ("ListenerDef[" + "listenerName=" +
                this.listenerName +
                ", listenerClass=" +
                this.listenerClass +
                "]");
    }
}
