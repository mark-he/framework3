package com.eagletsoft.boot.framework.common.tree;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class Node<T> implements Comparable<Node<T>>, Serializable {
	private Comparable id;
	private Comparable parentId;
	private T data;
	private Set<Node<T>> children = new TreeSet<>();
	
	public Node(Comparable id, Comparable parentId, T data) {
		super();
		this.id = id;
		this.parentId = parentId;
		this.data = data;
	}

	public Comparable getId() {
		return id;
	}

	public Comparable getParentId() {
		return parentId;
	}

	public void addChild(Node<T> child) {
		children.add(child);
	}

	public Collection<Node<T>> getChildren() {
		return children;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public void setId(Comparable id) {
		this.id = id;
	}
	public void setParentId(Comparable parentId) {
		this.parentId = parentId;
	}
	public void setChildren(Set<Node<T>> children) {
		this.children = children;
	}

	@Override
	public int compareTo(Node<T> o) {
		return id.compareTo(o.getId());
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
