package com.redhat.example.infinispan.remote;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.jboss.logging.Logger;

public class PartitionedCache<K,V> extends ParallelRemoteCache<K, V> {
	static Logger log = Logger.getLogger(ParallelRemoteCache.class);

	RemoteCacheManager manager;
	RemoteCache<?,?> defaultCache;
	int chunkSize = 100;
	AtomicInteger count = new AtomicInteger(0);
	Map<Integer,ParallelRemoteCache<Integer,String>> partitionMap = new ConcurrentHashMap<>();
	Map<Integer,CompletableFuture<Void>> partitionFutureMap = new ConcurrentHashMap<>();

	public PartitionedCache(RemoteCache<K,V> cache, BiPredicate<K,V> completionCondition, Runnable completionHandler) {
		super(cache, completionCondition, completionHandler);
	}
}
