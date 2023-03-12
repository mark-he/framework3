package com.eagletsoft.boot.framework.common.tree;

import java.io.Serializable;
import java.util.*;

/*Node must implements Comparable(not == 0) and HashCode and Equals*/
public class Tree<T> implements Serializable {
    private static final long serialVersionUID = 1L;
	private Map<Object, Node<T>> nodes = new HashMap<>();
	private Collection<Node<T>> children = new TreeSet<>();
	
	private static final int MAX = 999;

	public Tree() {
	}

	public Tree(Collection<Node<T>> children) {
		this.addAll(children);
	}

	public Collection<Node<T>> getAllNodes() {
		return nodes.values();
	}

	public Tree<T> add(Node<T> node)
	{
		nodes.put(node.getId(), node);
		return this;
	}
	
	public Tree<T> addAll(Collection<Node<T>> list)
	{
		for (Node<T> node : list) {
			nodes.put(node.getId(), node);
		}
		return this;
	}
	
	public Node<T> find(Object id)
	{
		return nodes.get(id);
	}
	
	public Map<Object, Node<T>> findAll() {
		return nodes;
	}

	public Collection<Node<T>> getChildren() {
		return children;
	}
	
	public List<Object> getAllParentIds(Object id, boolean includeSelf) {
		Node<T> node = find(id);
		
		List<Object> ids = new ArrayList<>();

		if (null == node) {
			return ids;
		}

		if (includeSelf) {
			ids.add(node.getId());
		}

		while(true) {
			if (null == node.getParentId()) {
				break;
			}
			else {
				ids.add(node.getParentId());
				node = find(node.getParentId());
				if (ids.size() > MAX) {
					throw new RuntimeException("Too many parents.");
				}
			}
		}
		return ids;
	}
	
	public List<Object> getAllChildrenIds(Object id, boolean includeSelf) {
		Node<T> node = find(id);
		List<Object> ids = new ArrayList<>();
		if(null == node) {
			return ids;	
		}
		if (includeSelf) {
			ids.add(node.getId());
		}
		
		scanChildren(ids, node);
		return ids;
	}
	

	public List<Node<T>> getAllChildren(Object id, boolean includeSelf) {
		Node<T> node = find(id);
		List<Node<T>> nodeSet = new ArrayList<>();
		if (includeSelf) {
			nodeSet.add(node);
		}
		scanChildrenNode(nodeSet, node);
		return nodeSet;
	}
	
	private void scanChildren(List<Object> ids, Node<T> node)
	{
		if (ids.size() > MAX) {
			throw new RuntimeException("Too many children.");
		}
		Collection<Node<T>> cs = node.getChildren();
		if (null != cs)
		{
			for (Node<T> sub : cs)
			{
				ids.add(sub.getId());
				scanChildren(ids, sub);
			}
		}
	}

	private void scanChildrenNode(List<Node<T>> nodeSet, Node<T> node)
	{
		Collection<Node<T>> cs = node.getChildren();
		if (null != cs)
		{
			for (Node<T> sub : cs)
			{
				nodeSet.add(sub);
				scanChildrenNode(nodeSet, sub);
			}
		}
	}
	
	public Tree<T> build()
	{
		for (Node<T> node : nodes.values())
		{
			if (null == node.getParentId())
			{
				children.add(node);
			}
			else
			{
				Node<T> parentNode = nodes.get(node.getParentId());
				if (null != parentNode)
				{
					parentNode.addChild(node);
				}
			}
		}
		return this;
	}
}
