package io.vertx.spi.cluster.hazelcast.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.spi.cluster.ChoosableIterable;

public class ChoosableSet<T> implements ChoosableIterable<T> {
	private volatile boolean initialised;
	private final Set<T> ids;
	private volatile Iterator<T> iter;

	public ChoosableSet(int initialSize) {
		ids = new ConcurrentHashSet<T>(initialSize);
	}

	public int size() {
		return ids.size();
	}

	public boolean isInitialised() {
		return initialised;
	}

	public void setInitialised() {
		this.initialised = true;
	}

	public void add(T elem) {
		ids.add(elem);
	}

	public void remove(T elem) {
		ids.remove(elem);
	}

	public void merge(ChoosableSet<T> toMerge) {
		ids.addAll(toMerge.ids);
	}

	public boolean isEmpty() {
		return ids.isEmpty();
	}

	public Iterator<T> iterator() {
		return ids.iterator();
	}

	public synchronized T choose() {
		if (!ids.isEmpty()) {
			if (iter == null || !iter.hasNext()) {
				iter = ids.iterator();
			}
			try {
				return iter.next();
			} catch (NoSuchElementException e) {
				return null;
			}
		} else {
			return null;
		}
	}
}
