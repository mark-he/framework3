package com.eagletsoft.boot.framework.common.errors;

public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private String namespace;
    private int status;
    private String key;
    private Object[] params;

    public ServiceException(Throwable cause)
    {
        super(cause);
        this.status = StandardErrors.UNKNOWN.getStatus();
        this.key = cause.getMessage();
    }

    public ServiceException(StandardErrors.ServerError error)
    {
        super(error.getMessage());
        this.status = error.getStatus();
        this.key = error.getMessage();
    }

    public ServiceException(int status, String key, Object... params)
    {
        super(key);
        this.status = status;
        this.key = key;
        this.params = params;
    }

    public Object[] getParams()
    {
        return params;
    }

    public int getStatus() {
        return status;
    }

    public String getKey() {
        return key;
    }

    public String getNamespace() {
        return namespace;
    }

    public ServiceException setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public ServiceException setStatus(int status) {
        this.status = status;
        return this;
    }

    public ServiceException setKey(String key) {
        this.key = key;
        return this;
    }

    public ServiceException setParams(Object[] params) {
        this.params = params;
        return this;
    }
}
