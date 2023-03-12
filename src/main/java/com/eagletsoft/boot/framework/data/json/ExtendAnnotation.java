package com.eagletsoft.boot.framework.data.json;

public class ExtendAnnotation {
    protected boolean raw;
    protected String name;
    protected Object mapping;
    public ExtendAnnotation(boolean raw, String name, Object mapping) {
        super();
        this.raw = raw;
        this.name = name;
        this.mapping = mapping;
    }
    public Object getMapping() {
        return mapping;
    }
    public void setMapping(Object mapping) {
        this.mapping = mapping;
    }
    public boolean isRaw() {
        return raw;
    }
    public void setRaw(boolean raw) {
        this.raw = raw;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
