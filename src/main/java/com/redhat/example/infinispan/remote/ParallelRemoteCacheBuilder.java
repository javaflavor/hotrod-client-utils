package com.redhat.example.infinispan.remote;

import java.util.function.BiPredicate;
import java.util.function.Consumer;

import org.infinispan.client.hotrod.RemoteCache;

public class ParallelRemoteCacheBuilder<K,V> {
	RemoteCache<K,V> cache;
	BiPredicate<K,V> completionCondition;
	Consumer<ParallelRemoteCache<K,V>> completionHandler;
	
	public ParallelRemoteCacheBuilder<K,V> cache(RemoteCache<K,V> cache) {
		this.cache = cache;
		return this;
	}
	
	public ParallelRemoteCacheBuilder<K,V> completionCondition(BiPredicate<K,V> completionCondition) {
		this.completionCondition = completionCondition;
		return this;
	}
	
	public ParallelRemoteCacheBuilder<K,V> completionHandler(Consumer<ParallelRemoteCache<K,V>> completionHandler) {
		this.completionHandler = completionHandler;
		return this;
	}
	
	public ParallelRemoteCache<K,V> build() {
		return new ParallelRemoteCache<K,V>(cache, completionCondition, completionHandler);
	}
	
}
