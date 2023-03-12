package com.eagletsoft.boot.framework.event;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class ByThreadEventDispatcher extends DefaultEventDispatcher {
	private static final ThreadLocal<List<Event>> PENDING_EVENTS = new ThreadLocal<>();

	@PostConstruct
	public void init()
	{
		queue = new LinkedBlockingQueue<Event>();
		pool = Executors.newFixedThreadPool(maxThreadNumber);
		running = true;
		new Thread(this).start();
		LOG.info("Started event dispatcher successfully.");
	}
	
	@Override
	public void dispatch(Event event) {
		super.executeCallbacks(event);
		List<Event> pending = PENDING_EVENTS.get();
		if (null == pending) {
			pending = new ArrayList<>();
			PENDING_EVENTS.set(pending);
		}
		pending.add(event);
	}
	
	public void start() {
		PENDING_EVENTS.remove();
	}
	
	public void rollback() {
		PENDING_EVENTS.remove();
	}
	
	public void commit() {
		List<Event> pending = PENDING_EVENTS.get();
		if (null != pending) {
			for (Event event : pending) {
				super.executeListeners(event);
			}
			PENDING_EVENTS.remove();
		}
	}
}
