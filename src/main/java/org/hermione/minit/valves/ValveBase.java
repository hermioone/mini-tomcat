package org.hermione.minit.valves;

import lombok.Getter;
import lombok.Setter;
import org.hermione.minit.Pipeline;
import org.hermione.minit.Valve;

public abstract class ValveBase implements Valve {
    @Getter
    private final String name;

    @Getter
    @Setter
    protected Pipeline pipeline = null;
    protected int debug = 0;
    protected static String info = "org.hermione.minit.valves.ValveBase/0.1";

    public ValveBase(Pipeline pipeline, String name) {
        this.pipeline = pipeline;
        this.name = name;
    }

    public int getDebug() {
        return (this.debug);
    }

    public void setDebug(int debug) {
        this.debug = debug;
    }

    public String getInfo() {
        return (info);
    }
}
