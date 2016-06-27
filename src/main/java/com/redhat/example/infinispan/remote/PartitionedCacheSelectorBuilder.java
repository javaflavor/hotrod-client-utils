package com.redhat.example.infinispan.remote;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;

public class PartitionedCacheSelectorBuilder<K,V> {
	String baseCacheName;
	RemoteCacheManager manager;
	BiFunction<K,V,String> partitionIdFunction;
	BiPredicate<K,V> partitionCompletionPredicate;
	Runnable partitionCompletionHandler;
	
	public PartitionedCacheSelectorBuilder<K,V> baseCacheName(String baseCacheName) {
		this.baseCacheName = baseCacheName;
		return this;
	}
	
	public PartitionedCacheSelectorBuilder<K,V> cacheManager(RemoteCacheManager manager) {
		this.manager = manager;
		return this;
	}
	
	public PartitionedCacheSelectorBuilder<K,V> partitionIdFunction(BiFunction<K,V,String> partitionIdFunction) {
		this.partitionIdFunction = partitionIdFunction;
		return this;
	}
	
	public PartitionedCacheSelectorBuilder<K,V> partitionCompletionPredicate(BiPredicate<K,V> partitionCompletionPredicate) {
		this.partitionCompletionPredicate = partitionCompletionPredicate;
		return this;
	}

	public PartitionedCacheSelectorBuilder<K,V> partitionCompletionHandler(Runnable partitionCompletionHandler) {
		this.partitionCompletionHandler = partitionCompletionHandler;
		return this;
	}
	
	public PartitionedCacheSelector<K,V> build() {
		return new PartitionedCacheSelector<K,V>(
				baseCacheName, manager, partitionIdFunction, partitionCompletionPredicate, partitionCompletionHandler);
	}
	
}
