package io.vertx.spi.cluster.hazelcast.impl;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.MapEvent;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.spi.cluster.AsyncMultiMap;
import io.vertx.core.spi.cluster.ChoosableIterable;

public class HazelcastAsyncMultiMap<K, V> implements AsyncMultiMap<K, V>, EntryListener<K, V>{

	private static final Logger log = LoggerFactory.getLogger(HazelcastAsyncMultiMap.class);

	private final Vertx vertx;
	private final com.hazelcast.core.MultiMap<K, V> map;
	private final AtomicInteger getInProgressCount = new AtomicInteger();

	/*
	   The Hazelcast near cache is very slow so we use our own one.
	   Keeping it in sync is a little tricky. As entries are added or removed the EntryListener will be called
	   but when the node joins the cluster it isn't provided the initial state via the EntryListener
	   Therefore the first time get is called for a subscription we *always* get the subs from
	   Hazelcast (this is what the initialised flag is for), then consider that the initial state.
	   While the get is in progress the entry listener may be being called, so we merge any
	   pre-existing entries so we don't lose any. Hazelcast doesn't seem to have any consistent
	   way to get an initial state plus a stream of updates.
	 */
	private ConcurrentMap<K, ChoosableSet<V>> cache = new ConcurrentHashMap<>();

	public HazelcastAsyncMultiMap(Vertx vertx, com.hazelcast.core.MultiMap<K, V> map) {
		this.vertx = vertx;
		this.map = map;
		map.addEntryListener(this, true);
	}

	@Override
	public void entryAdded(EntryEvent<K, V> entry) {
		addEntry(entry.getKey(), entry.getValue());

	}

	@Override
	public void entryUpdated(EntryEvent<K, V> entry) {
		K k = entry.getKey();
	    ChoosableSet<V> entries = cache.get(k);
	    if (entries != null) {
	      entries.add(entry.getValue());
	    }
	}

	@Override
	public void entryRemoved(EntryEvent<K, V> entry) {
		removeEntry(entry.getKey(), entry.getOldValue());

	}

	@Override
	public void entryEvicted(EntryEvent<K, V> entry) {
		 entryRemoved(entry);

	}

	@Override
	public void mapCleared(MapEvent event) {
		cache.clear();

	}

	@Override
	public void mapEvicted(MapEvent event) {
		cache.clear();

	}

	@Override
	public void add(K k, V v, Handler<AsyncResult<Void>> completionHandler) {
		vertx.executeBlocking(fut -> {
			map.put(k, HazelcastServerID.convertServerID(v));
			fut.complete();
		}, completionHandler);

	}

	@Override
	public void get(K k, Handler<AsyncResult<ChoosableIterable<V>>> resultHandler) {
		ChoosableSet<V> entries = cache.get(k);
		if (entries != null && entries.isInitialised() && getInProgressCount.get() == 0) {
			resultHandler.handle(Future.succeededFuture(entries));
		} else {
			getInProgressCount.incrementAndGet();
			vertx.<ChoosableIterable<V>>executeBlocking(fut -> {
				Collection<V> entries2 = map.get(k);
				ChoosableSet<V> sids;
				if (entries2 != null) {
					sids = new ChoosableSet<>(entries2.size());
					for (V hid : entries2) {
						sids.add(hid);
					}
				} else {
					sids = new ChoosableSet<>(0);
				}
				ChoosableSet<V> prev = (sids.isEmpty()) ? null : cache.putIfAbsent(k, sids);
				if (prev != null) {
					// Merge them
					prev.merge(sids);
					sids = prev;
				}
				sids.setInitialised();
				fut.complete(sids);
			}, res -> { getInProgressCount.decrementAndGet();
			resultHandler.handle(res);
			});
		}
	}

	@Override
	public void remove(K k, V v, Handler<AsyncResult<Boolean>> completionHandler) {
		vertx.executeBlocking(fut -> fut.complete(map.remove(k, HazelcastServerID.convertServerID(v))), completionHandler);

	}

	@Override
	public void removeAllForValue(V val, Handler<AsyncResult<Void>> completionHandler) {
		vertx.executeBlocking(fut -> {
			for (Map.Entry<K, V> entry : map.entrySet()) {
				V v = entry.getValue();
				if (val.equals(v)) {
					map.remove(entry.getKey(), v);
				}
			}
			fut.complete();
		}, completionHandler);

	}

	private void addEntry(K k, V v) {
		ChoosableSet<V> entries = cache.get(k);
		if (entries == null) {
			entries = new ChoosableSet<>(1);
			ChoosableSet<V> prev = cache.putIfAbsent(k, entries);
			if (prev != null) {
				entries = prev;
			}
		}
		entries.add(v);
	}

	private void removeEntry(K k, V v) {
		ChoosableSet<V> entries = cache.get(k);
		if (entries != null) {
			// We forbid `null` values, but it can comes from another application using Hazelcast
			// (but not in the context of vert.x)
			if (v != null) {
				entries.remove(v);
				if (entries.isEmpty()) {
					cache.remove(k);
				}
			}
		}
	}
}
