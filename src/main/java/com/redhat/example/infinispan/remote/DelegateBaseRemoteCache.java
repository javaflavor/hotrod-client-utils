package com.redhat.example.infinispan.remote;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.infinispan.client.hotrod.CacheTopologyInfo;
import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.MetadataValue;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.ServerStatistics;
import org.infinispan.client.hotrod.VersionedValue;
import org.infinispan.commons.util.CloseableIterator;
import org.infinispan.query.dsl.Query;

public class DelegateBaseRemoteCache<K,V> implements RemoteCache<K,V> {
	RemoteCache<K,V> delegate;

	public DelegateBaseRemoteCache(RemoteCache<K,V> delegate) {
		this.delegate = delegate;
	}

	public void addClientListener(Object arg0, Object[] arg1, Object[] arg2) {
		delegate.addClientListener(arg0, arg1, arg2);
	}

	public void addClientListener(Object arg0) {
		delegate.addClientListener(arg0);
	}

	public V getOrDefault(Object key, V defaultValue) {
		return delegate.getOrDefault(key, defaultValue);
	}

	public boolean containsKey(Object key) {
		return delegate.containsKey(key);
	}

	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		return delegate.computeIfAbsent(key, mappingFunction);
	}

	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return delegate.computeIfPresent(key, remappingFunction);
	}

	public void clear() {
		delegate.clear();
	}

	public CompletableFuture<Void> clearAsync() {
		return delegate.clearAsync();
	}

	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return delegate.compute(key, remappingFunction);
	}

	public boolean containsValue(Object arg0) {
		return delegate.containsValue(arg0);
	}

	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return delegate.entrySet();
	}

	public V get(Object key) {
		return delegate.get(key);
	}

	public boolean equals(Object o) {
		return delegate.equals(o);
	}

	public <T> T execute(String arg0, Map<String, ?> arg1) {
		return delegate.execute(arg0, arg1);
	}

	public void forEach(BiConsumer<? super K, ? super V> action) {
		delegate.forEach(action);
	}

	public Map<K, V> getAll(Set<? extends K> arg0) {
		return delegate.getAll(arg0);
	}

	public CompletableFuture<V> getAsync(K arg0) {
		return delegate.getAsync(arg0);
	}

	@Deprecated
	public Map<K, V> getBulk() {
		return delegate.getBulk();
	}

	@Deprecated
	public Map<K, V> getBulk(int arg0) {
		return delegate.getBulk(arg0);
	}

	public CacheTopologyInfo getCacheTopologyInfo() {
		return delegate.getCacheTopologyInfo();
	}

	public Set<Object> getListeners() {
		return delegate.getListeners();
	}

	public String getName() {
		return delegate.getName();
	}

	public String getProtocolVersion() {
		return delegate.getProtocolVersion();
	}

	public RemoteCacheManager getRemoteCacheManager() {
		return delegate.getRemoteCacheManager();
	}

	public String getVersion() {
		return delegate.getVersion();
	}

	@Deprecated
	public VersionedValue<V> getVersioned(K arg0) {
		return delegate.getVersioned(arg0);
	}

	public MetadataValue<V> getWithMetadata(K arg0) {
		return delegate.getWithMetadata(arg0);
	}

	public int hashCode() {
		return delegate.hashCode();
	}

	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	public Set<K> keySet() {
		return delegate.keySet();
	}

	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		return delegate.merge(key, value, remappingFunction);
	}

	public V put(K arg0, V arg1, long arg2, TimeUnit arg3, long arg4, TimeUnit arg5) {
		return delegate.put(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	public V put(K arg0, V arg1, long arg2, TimeUnit arg3) {
		return delegate.put(arg0, arg1, arg2, arg3);
	}

	public V put(K arg0, V arg1) {
		return delegate.put(arg0, arg1);
	}

	public void putAll(Map<? extends K, ? extends V> arg0, long arg1, TimeUnit arg2, long arg3, TimeUnit arg4) {
		delegate.putAll(arg0, arg1, arg2, arg3, arg4);
	}

	public void putAll(Map<? extends K, ? extends V> arg0, long arg1, TimeUnit arg2) {
		delegate.putAll(arg0, arg1, arg2);
	}

	public void putAll(Map<? extends K, ? extends V> arg0) {
		delegate.putAll(arg0);
	}

	public CompletableFuture<Void> putAllAsync(Map<? extends K, ? extends V> arg0, long arg1, TimeUnit arg2, long arg3,
			TimeUnit arg4) {
		return delegate.putAllAsync(arg0, arg1, arg2, arg3, arg4);
	}

	public CompletableFuture<Void> putAllAsync(Map<? extends K, ? extends V> arg0, long arg1, TimeUnit arg2) {
		return delegate.putAllAsync(arg0, arg1, arg2);
	}

	public CompletableFuture<Void> putAllAsync(Map<? extends K, ? extends V> arg0) {
		return delegate.putAllAsync(arg0);
	}

	public CompletableFuture<V> putAsync(K arg0, V arg1, long arg2, TimeUnit arg3, long arg4, TimeUnit arg5) {
		return delegate.putAsync(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	public CompletableFuture<V> putAsync(K arg0, V arg1, long arg2, TimeUnit arg3) {
		return delegate.putAsync(arg0, arg1, arg2, arg3);
	}

	public CompletableFuture<V> putAsync(K arg0, V arg1) {
		return delegate.putAsync(arg0, arg1);
	}

	public V putIfAbsent(K arg0, V arg1, long arg2, TimeUnit arg3, long arg4, TimeUnit arg5) {
		return delegate.putIfAbsent(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	public V putIfAbsent(K arg0, V arg1, long arg2, TimeUnit arg3) {
		return delegate.putIfAbsent(arg0, arg1, arg2, arg3);
	}

	public V putIfAbsent(K key, V value) {
		return delegate.putIfAbsent(key, value);
	}

	public CompletableFuture<V> putIfAbsentAsync(K arg0, V arg1, long arg2, TimeUnit arg3, long arg4, TimeUnit arg5) {
		return delegate.putIfAbsentAsync(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	public CompletableFuture<V> putIfAbsentAsync(K arg0, V arg1, long arg2, TimeUnit arg3) {
		return delegate.putIfAbsentAsync(arg0, arg1, arg2, arg3);
	}

	public CompletableFuture<V> putIfAbsentAsync(K arg0, V arg1) {
		return delegate.putIfAbsentAsync(arg0, arg1);
	}

	public boolean remove(Object key, Object value) {
		return delegate.remove(key, value);
	}

	public V remove(Object arg0) {
		return delegate.remove(arg0);
	}

	public CompletableFuture<Boolean> removeAsync(Object arg0, Object arg1) {
		return delegate.removeAsync(arg0, arg1);
	}

	public CompletableFuture<V> removeAsync(Object arg0) {
		return delegate.removeAsync(arg0);
	}

	public void removeClientListener(Object arg0) {
		delegate.removeClientListener(arg0);
	}

	public boolean removeWithVersion(K arg0, long arg1) {
		return delegate.removeWithVersion(arg0, arg1);
	}

	public CompletableFuture<Boolean> removeWithVersionAsync(K arg0, long arg1) {
		return delegate.removeWithVersionAsync(arg0, arg1);
	}

	public V replace(K arg0, V arg1, long arg2, TimeUnit arg3, long arg4, TimeUnit arg5) {
		return delegate.replace(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	public V replace(K arg0, V arg1, long arg2, TimeUnit arg3) {
		return delegate.replace(arg0, arg1, arg2, arg3);
	}

	public boolean replace(K arg0, V arg1, V arg2, long arg3, TimeUnit arg4, long arg5, TimeUnit arg6) {
		return delegate.replace(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
	}

	public boolean replace(K arg0, V arg1, V arg2, long arg3, TimeUnit arg4) {
		return delegate.replace(arg0, arg1, arg2, arg3, arg4);
	}

	public boolean replace(K key, V oldValue, V newValue) {
		return delegate.replace(key, oldValue, newValue);
	}

	public V replace(K key, V value) {
		return delegate.replace(key, value);
	}

	public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		delegate.replaceAll(function);
	}

	public CompletableFuture<V> replaceAsync(K arg0, V arg1, long arg2, TimeUnit arg3, long arg4, TimeUnit arg5) {
		return delegate.replaceAsync(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	public CompletableFuture<V> replaceAsync(K arg0, V arg1, long arg2, TimeUnit arg3) {
		return delegate.replaceAsync(arg0, arg1, arg2, arg3);
	}

	public CompletableFuture<Boolean> replaceAsync(K arg0, V arg1, V arg2, long arg3, TimeUnit arg4, long arg5,
			TimeUnit arg6) {
		return delegate.replaceAsync(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
	}

	public CompletableFuture<Boolean> replaceAsync(K arg0, V arg1, V arg2, long arg3, TimeUnit arg4) {
		return delegate.replaceAsync(arg0, arg1, arg2, arg3, arg4);
	}

	public CompletableFuture<Boolean> replaceAsync(K arg0, V arg1, V arg2) {
		return delegate.replaceAsync(arg0, arg1, arg2);
	}

	public CompletableFuture<V> replaceAsync(K arg0, V arg1) {
		return delegate.replaceAsync(arg0, arg1);
	}

	public boolean replaceWithVersion(K arg0, V arg1, long arg2, int arg3, int arg4) {
		return delegate.replaceWithVersion(arg0, arg1, arg2, arg3, arg4);
	}

	public boolean replaceWithVersion(K arg0, V arg1, long arg2, int arg3) {
		return delegate.replaceWithVersion(arg0, arg1, arg2, arg3);
	}

	public boolean replaceWithVersion(K arg0, V arg1, long arg2, long arg3, TimeUnit arg4, long arg5, TimeUnit arg6) {
		return delegate.replaceWithVersion(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
	}

	public boolean replaceWithVersion(K arg0, V arg1, long arg2) {
		return delegate.replaceWithVersion(arg0, arg1, arg2);
	}

	public CompletableFuture<Boolean> replaceWithVersionAsync(K arg0, V arg1, long arg2, int arg3, int arg4) {
		return delegate.replaceWithVersionAsync(arg0, arg1, arg2, arg3, arg4);
	}

	public CompletableFuture<Boolean> replaceWithVersionAsync(K arg0, V arg1, long arg2, int arg3) {
		return delegate.replaceWithVersionAsync(arg0, arg1, arg2, arg3);
	}

	public CompletableFuture<Boolean> replaceWithVersionAsync(K arg0, V arg1, long arg2) {
		return delegate.replaceWithVersionAsync(arg0, arg1, arg2);
	}

	public CloseableIterator<java.util.Map.Entry<Object, Object>> retrieveEntries(String arg0, int arg1) {
		return delegate.retrieveEntries(arg0, arg1);
	}

	public CloseableIterator<java.util.Map.Entry<Object, Object>> retrieveEntries(String arg0, Object[] arg1,
			Set<Integer> arg2, int arg3) {
		return delegate.retrieveEntries(arg0, arg1, arg2, arg3);
	}

	public CloseableIterator<java.util.Map.Entry<Object, Object>> retrieveEntries(String arg0, Set<Integer> arg1,
			int arg2) {
		return delegate.retrieveEntries(arg0, arg1, arg2);
	}

	public CloseableIterator<java.util.Map.Entry<Object, Object>> retrieveEntriesByQuery(Query arg0, Set<Integer> arg1,
			int arg2) {
		return delegate.retrieveEntriesByQuery(arg0, arg1, arg2);
	}

	public CloseableIterator<java.util.Map.Entry<Object, MetadataValue<Object>>> retrieveEntriesWithMetadata(
			Set<Integer> arg0, int arg1) {
		return delegate.retrieveEntriesWithMetadata(arg0, arg1);
	}

	public int size() {
		return delegate.size();
	}

	public void start() {
		delegate.start();
	}

	public ServerStatistics stats() {
		return delegate.stats();
	}

	public void stop() {
		delegate.stop();
	}

	public Collection<V> values() {
		return delegate.values();
	}

	public RemoteCache<K, V> withFlags(Flag... arg0) {
		return delegate.withFlags(arg0);
	}
}
