package com.eagletsoft.boot.framework.event;

import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class DefaultEventDispatcher implements IEventDispatcher, Runnable
{
	protected static Logger LOG = org.slf4j.LoggerFactory.getLogger(IEventDispatcher.class);
	protected boolean running;
	protected int maxThreadNumber = 3;
	
	protected BlockingQueue<Event> queue;
	protected ExecutorService pool;
	protected Map<String, List<IEventListener>> listeners = new HashMap<String, List<IEventListener>>();
	protected Map<String, List<IEventCallback>> callbacks = new HashMap<String, List<IEventCallback>>();
	
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
    public void dispatch(Event event) 
    {
		this.executeCallbacks(event);
	    this.executeListeners(event);
    }
	
	protected void executeListeners(Event event) {
		queue.add(event);
	}
	
	protected void executeCallbacks(Event event) 
	{
		List<IEventCallback> list = callbacks.get(event.getCode());
		if (null != list)
		{
			int i = 0;
			while(i < list.size())
			{
				list.get(i).process(event);
    			i++;
			}
		}
	}
	
	@Override
    public void run()
    {
		Event event = null;
	    while(true)
	    {
	    	if (!running)
	    	{
		    	break;
	    	}
	    	try
            {
	            event = queue.take();
	    		List<IEventListener> list = listeners.get(event.getCode());
	    		if (null != list)
	    		{
	    			int i = 0;
	    			while(i < list.size())
	    			{
	    				try
	    				{
	    					pool.execute(new EventProcessor(event, list.get(i)));
	    				}
	    				catch (Exception ex)
	    				{
	    		            LOG.info("Error in processing event listener", ex);
	    				}
		    			i++;
	    			}
	    		}
	            
            } catch (InterruptedException e)
            {
	            LOG.info("Error in processing events", e);
            }
	    }
		pool.shutdown();
    }

	@Override
    public synchronized void addListener(String code, IEventListener listener)
    {
		List<IEventListener> list = listeners.get(code);
		if (null == list)
		{
			list = new LinkedList<IEventListener>();
			listeners.put(code, list);
		}
		list.add(listener);
    }

	@Override
    public synchronized void removeListener(String code, IEventListener listener)
    {
		List<IEventListener> list = listeners.get(code);
		if (null != list)
		{
			list.remove(listener);
		}
    }
	
	
	@Override
    public synchronized void addCallback(String code, IEventCallback callback)
    {
		List<IEventCallback> list = callbacks.get(code);
		if (null == list)
		{
			list = new LinkedList<IEventCallback>();
			callbacks.put(code, list);
		}
		list.add(callback);
    }

	@Override
    public synchronized void removeCallback(String code, IEventCallback callback)
    {
		List<IEventCallback> list = callbacks.get(code);
		if (null != list)
		{
			list.remove(callback);
		}
    }

	public void close()
	{
		running = false;
	}
	
	static class EventProcessor implements Runnable
	{
		private Event event;
		private IEventListener listener;
		
		public EventProcessor(Event event, IEventListener listener)
		{
			this.event = event;
			this.listener = listener;
		}

		@Override
        public void run()
        {
			listener.check(event);
        }
	}

	public Map<String, List<IEventListener>> getListeners()
	{
		return listeners;
	}

	public void setListeners(Map<String, List<IEventListener>> listeners)
	{
		this.listeners = listeners;
	}

	public int getMaxThreadNumber()
	{
		return maxThreadNumber;
	}

	public void setMaxThreadNumber(int maxThreadNumber)
	{
		this.maxThreadNumber = maxThreadNumber;
	}
	
}
