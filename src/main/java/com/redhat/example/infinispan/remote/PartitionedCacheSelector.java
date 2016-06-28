package com.redhat.example.infinispan.remote;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.jboss.logging.Logger;

public class PartitionedCacheSelector<K,V> {
	static Logger log = Logger.getLogger(PartitionedCacheSelector.class);

	String baseCacheName;
	RemoteCacheManager manager;
	Optional<BiFunction<K,V,String>> partitionIdFunction;
	Optional<BiPredicate<K,V>> partitionCompletionPredicate;
	Consumer<ParallelRemoteCache<K,V>> partitionCompletionHandler;
	RemoteCache<?,?> defaultCache;
	int chunkSize = 100;
	AtomicInteger count = new AtomicInteger(0);
	NavigableMap<String,ParallelRemoteCache<K,V>> partitionMap = new ConcurrentSkipListMap<>();
	NavigableMap<String,CompletableFuture<Void>> partitionFutureMap = new ConcurrentSkipListMap<>();

	public PartitionedCacheSelector(String baseCacheName,
			RemoteCacheManager manager,
			BiFunction<K,V,String> partitionIdFunction,
			BiPredicate<K,V> partitionCompletionPredicate,
			Consumer<ParallelRemoteCache<K,V>> partitionCompletionHandler) {
		this.baseCacheName = baseCacheName;
		this.manager = manager;
		this.partitionIdFunction = Optional.ofNullable(partitionIdFunction);
		this.partitionCompletionPredicate = Optional.ofNullable(partitionCompletionPredicate);
		this.partitionCompletionHandler = partitionCompletionHandler;
		defaultCache = manager.getCache();
	}
	
	public ParallelRemoteCache<K,V> getLastPartitionedCache() {
		return partitionMap.lastEntry().getValue();
	}
	
	public CompletableFuture<Void> getLastPartitionedCacheFuture() {
		return partitionFutureMap.lastEntry().getValue();
	}

	public ParallelRemoteCache<K,V> getPartitionedCache(K key, V value) {
		log.tracef("### getPartitionedCache(%s, %s) called.", key, value);
		String partitionId = partitionIdFunction.map(f -> f.apply(key, value)).orElse("");
		log.tracef("partitionId = %s", partitionId);
		ParallelRemoteCache<K,V> cache = partitionMap.get(partitionId);
		if (cache == null) {
			CompletableFuture<Void> future;
			synchronized (partitionFutureMap) {
				if ((future = partitionFutureMap.get(partitionId)) == null) {
					// Create new partition, asynchronously.
					future = createPartitionedCacheFuture(partitionId);
				}
			}
			// Wait the partition creation.
			future.join();
			cache = partitionMap.get(partitionId);
		}
		return cache;
	}
	
	CompletableFuture<Void> createPartitionedCacheFuture(String partitionId) {
		String partitionedCacheName = baseCacheName + "#" + partitionId;
		log.debugf("Creating new partitioned cache: %s", partitionedCacheName);
		// Create new partitioned cache, asynchronously.
		CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
			RemoteCache<K,V> basecache;
			if (existsCache(partitionedCacheName)) {
				basecache = manager.getCache(partitionedCacheName);
			} else {
				// Create new partitioned cache.
				System.out.println("Creating new partioned cache: "+partitionedCacheName);
				defaultCache.execute("manage-cache", new HashMap<String,String>() {{
					put("command", "create"); put("cacheName", partitionedCacheName);
				}});
				basecache = manager.getCache(partitionedCacheName);
				basecache.start();
			}
			// Wrap with ParallelRemoteCache.
			ParallelRemoteCache<K,V> cache = new ParallelRemoteCacheBuilder<K,V>()
					.cache(basecache)
					.completionCondition(partitionCompletionPredicate.orElse(null))
					.completionHandler(partitionCompletionHandler)
					.build();
			partitionMap.put(partitionId, cache);
//			partitionFutureMap.remove(partitionId);		// Is it safe? maybe.
		});
		partitionFutureMap.put(partitionId, future);
		return future;
	}
	
	boolean existsCache(String name) {
		Set<String> cacheNames = defaultCache.execute("list-cache", null);
		return cacheNames.contains(name);
	}
}
