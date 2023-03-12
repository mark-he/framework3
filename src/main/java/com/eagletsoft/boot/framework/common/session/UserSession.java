package com.eagletsoft.boot.framework.common.session;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.*;

public class UserSession {
	private static ThreadLocal<Object> authorizeHolder = new ThreadLocal<>();
	private static ThreadLocal<Locale> localeHolder = new ThreadLocal<>();
	
	public static void setLocale(Locale locale) {
		localeHolder.set(locale);
	}
	
	public static Locale getLocale() {
		return localeHolder.get();
	}

	public static <T extends UserInterface> void setAuthorize(String token, T user, Collection<String> permissions) {
		Authorize<T> auth = new Authorize<>(token, user, permissions, Collections.EMPTY_MAP);
		authorizeHolder.set(auth);
	}
	
	public static <T extends UserInterface> void setAuthorize(String token, T user, Collection<String> permissions, Map<String, Object> settings) {
		Authorize<T> auth = new Authorize<>(token, user, permissions, settings);
		authorizeHolder.set(auth);
	}
	
	public static <T extends UserInterface> void setAuthorize(Authorize<T> auth) {
		authorizeHolder.set(auth);
	}
	
	public static <T extends UserInterface> Authorize<T> getAuthorize() {
		Authorize<T> auth = (Authorize<T>)authorizeHolder.get();
		return auth;
	}
	
	public static <T extends UserInterface> T getUserInterface() {
		Authorize<T> auth = (Authorize<T>)authorizeHolder.get();
		if (null == auth) {
			return null;
		}
		return auth.getUser();
	}

	public static Collection<String> getPermisssions() {
		Authorize<?> auth = (Authorize<?>)authorizeHolder.get();
		Collection<String> permissions = null;
		if (null != auth) {
			permissions = auth.getPermissions();
		}
		if (null != permissions) {
			return permissions;
		}
		else {
			return Collections.EMPTY_SET;
		}
	}
	
	public static String findPermission(String access) {
		String[] accessArr = access.split(",", -1);
		for (int i = 0; i < accessArr.length; i++) {
			String perm = findOnePermission(accessArr[i].trim());
			if (null != perm) {
				return perm;
			}
		}
		return null;
	}

	private static String findOnePermission(String access) {
		Collection<String> permissions = getPermisssions();
		String[] accessSplit = access.split(":", -1);
		
		for (String p : permissions) {
			String[] pSplit = p.split(":", -1);
			if (pSplit[0].equals("*") || pSplit[0].equals(accessSplit[0])) {
				if (pSplit[1].equals("*") || pSplit[1].equals(accessSplit[1])) {
					return p;
				}
			}
		}
		return null;
	}
	
	
	public static void clear() {
		authorizeHolder.remove();
		localeHolder.remove();
	}
	
	public static class Authorize<T extends UserInterface> implements Serializable {
    	private String token;
    	private String clientId;
    	private T user;
    	private Collection<String> permissions = new HashSet<>();
    	private Map<String, Object> settings;
    	private String scope;
    	private String resource;

    	public Authorize() {
    		
    	}
    	
    	public Authorize(T user) {
    		this.user = user;
    	}

    	public Authorize(T user, Collection<String> permissions) {
    		this.user = user;
			this.permissions = parsePermissions(permissions);
    	}

    	public Authorize(String token, T user, Collection<String> permissions) {
    		this.token = token;
    		this.user = user;
			this.permissions = parsePermissions(permissions);
    	}
    	
    	public Authorize(String token, T user, Collection<String> permissions, Map<String, Object> settings) {
    		this.token = token;
    		this.user = user;
    		this.settings = settings;
			this.permissions = parsePermissions(permissions);
    	}

    	public static Set<String> parsePermission(String p) {
			Set<String> permSet = new HashSet<>();
			String[] ps = p.split(",", -1);
			for (String ps1 : ps) {
				int idx = ps1.indexOf(':');
				if (idx >= 0) {
					String base = ps1.substring(0, idx);
					String suffix = ps1.substring(idx + 1);

					String[] ps1Arr = suffix.split("\\|", -1);
					for (String ps1ArrItem : ps1Arr) {
						permSet.add(base + ":" + ps1ArrItem.trim());
					}
				}
			}
			return permSet;
		}

    	public static Set<String> parsePermissions(Collection<String> permissions) {
			Set<String> permSet = new HashSet<>();
			for (String p : permissions) {
				permSet.addAll(parsePermission(p));
			}
			return permSet;
		}

    	@JsonIgnore
    	public Object getUserId() {
    		if (null == user) {
    			return null;
    		}
    		else {
    			return user.getId();
    		}
    	}
    	
		public String getToken() {
			return token;
		}
		
		public void setToken(String token) {
			this.token = token;
		}
		
		public T getUser() {
			return user;
		}
		
		public void setUser(T user) {
			this.user = user;
		}
		
		public Collection<String> getPermissions() {
			return permissions;
		}
		
		public void setPermissions(Collection<String> permissions) {
			this.permissions = parsePermissions(permissions);
		}

		public Map<String, Object> getSettings() {
			return settings;
		}

		public void setSettings(Map<String, Object> settings) {
			this.settings = settings;
		}

		public String getClientId() {
			return clientId;
		}

		public void setClientId(String clientId) {
			this.clientId = clientId;
		}

		public String getScope() {
			return scope;
		}

		public void setScope(String scope) {
			this.scope = scope;
		}

		public String getResource() {
			return resource;
		}

		public void setResource(String resource) {
			this.resource = resource;
		}
	}
}
