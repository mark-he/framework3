package com.eagletsoft.boot.framework.data.json.context;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ExtViewContext {
	private static Logger LOG = LoggerFactory.getLogger(ExtViewContext.class);
	private static ThreadLocal<Context> CONTEXT = new ThreadLocal<>();
	
	public static void addExtend(Object object, String key, Object value) {
		LOG.debug("Add Extend for  " + object);

		ExtViewContext.track(object);
		Context ctx = CONTEXT.get();
		Collection<KeyValue> extendSet = (TreeSet<KeyValue>)ctx.keep.get(object.toString());
		
		if (null == extendSet) {
			extendSet = new TreeSet<>();
			ctx.keep.put(object.toString(), extendSet);
		}
		extendSet.add(new KeyValue(key, value));
	}

	public static Collection<KeyValue> getExtends(Object object) {
		LOG.debug("get Extend for  " + object);
		
		Context ctx = CONTEXT.get();
		Collection<KeyValue> extendSet = (Collection<KeyValue>)ctx.keep.get(object.toString());
		if (null == extendSet) {
			extendSet = new TreeSet<>();
			ctx.keep.put(object.toString(), extendSet);
		}
		return extendSet;
	}
	
	public static void track(Object object) {
		Context ctx = CONTEXT.get();
		if (ctx.track.contains(object.toString())) {
			ctx.duplicateTrack++;
		}
		else {
			ctx.track.add(object.toString());
		}
	}
	
	public static Context get() {
		Context context = CONTEXT.get();
		if (null == context) {
			context = new Context();
			CONTEXT.set(context);
		}
		return context;
	}
	
	public static boolean isTrack(Object obj) {
		Context ctx = CONTEXT.get();
		return ctx.track.contains(obj.toString());
	}

	public static void init() {
		CONTEXT.set(new Context());
	}

	public static void destroy() {
		CONTEXT.remove();
	}

	public static Context saveAndInit() {
		Context context = get();
		init();
		return context;
	}

	public static void destroyAndRestore(Context context) {
		destroy();
		CONTEXT.set(context);
	}
	
	public static class Context {
		private Map<String, Object> keep = new HashMap<>();
		private Set<Object> track = new HashSet<>();
		private int duplicateTrack = 0;
		private int level;
		private int maxDepth = 1;
		private boolean insideCollection;
		private String group;

		public int getMaxDepth() {
			return maxDepth;
		}

		public void setMaxDepth(int maxDepth) {
			this.maxDepth = maxDepth;
		}

		public Map<String, Object> getKeep() {
			return keep;
		}
		public void setKeep(Map<String, Object> keep) {
			this.keep = keep;
		}
		public Set<Object> getTrack() {
			return track;
		}
		public void setTrack(Set<Object> track) {
			this.track = track;
		}
		
		public void addLevel() {
			level++;
		}
		
		public void subLevel() {
			level--;
		}
		
		public int getLevel() {
			return level;
		}
		public void setLevel(int level) {
			this.level = level;
		}
		public boolean isInsideCollection() {
			return insideCollection;
		}
		public void setInsideCollection(boolean insideCollection) {
			this.insideCollection = insideCollection;
		}
		public boolean inViewport;

		public int getDuplicateTrack() {
			return duplicateTrack;
		}

		public void setDuplicateTrack(int duplicateTrack) {
			this.duplicateTrack = duplicateTrack;
		}

		public String getGroup() {
			if (StringUtils.isEmpty(group)) {
				return "default";
			}
			return group;
		}

		public void setGroup(String group) {
			this.group = group;
		}

		public boolean isInViewport() {
			return inViewport;
		}

		public void setInViewport(boolean inViewport) {
			this.inViewport = inViewport;
		}

		public boolean isInGroup(String[] groups) {
			return  inViewport || isDefault(groups) || ArrayUtils.contains(groups, group);
		}

		private static boolean isDefault(String[] groups) {
			return (null == groups || groups.length == 0 || ArrayUtils.contains(groups, "default"));
		}
	}
}
