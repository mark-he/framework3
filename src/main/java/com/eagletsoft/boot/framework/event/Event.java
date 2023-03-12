package com.eagletsoft.boot.framework.event;

import java.io.Serializable;

public class Event implements Serializable
{
	private static final long serialVersionUID = 1L;
	private String code;
	private Serializable source;
	private Serializable data;
	public Event(String code, Serializable source, Serializable data)
    {
	    this.code = code;
	    this.data = data;
	    this.source = source;
    }
	public Event(String code, Serializable data)
    {
	    this.code = code;
	    this.data = data;
    }
	public String getCode()
	{
		return code;
	}
	public Object getSource()
	{
		return source;
	}
	public Object getData()
	{
		return data;
	}
}
