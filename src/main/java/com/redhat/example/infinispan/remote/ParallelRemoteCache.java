package com.redhat.example.infinispan.remote;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import org.infinispan.client.hotrod.RemoteCache;
import org.jboss.logging.Logger;

public class ParallelRemoteCache<K,V> extends DelegateBaseRemoteCache<K,V> {
	static Logger log = Logger.getLogger(ParallelRemoteCache.class);
	
	Optional<BiPredicate<K,V>> completionCondition;
	Runnable completionHandler;
	Set<CompletableFuture<?>> futures = new CopyOnWriteArraySet<>();
	int collectFutureThreshold = 50;
	AtomicBoolean isCollectProcessing = new AtomicBoolean(false);	// may be reset
	AtomicBoolean isCompleting = new AtomicBoolean(false);			// to be reset
	CompletableFuture<Void> lastCompletableFuture;					// to be reset

	public ParallelRemoteCache(RemoteCache<K,V> cache) {
		super(cache);
	}

	public ParallelRemoteCache(RemoteCache<K,V> cache, BiPredicate<K,V> completionCondition, Runnable completionHandler) {
		this(cache);
		this.completionCondition = Optional.ofNullable(completionCondition);
		this.completionHandler = completionHandler;
	}

	public CompletableFuture<Void> getLastCompletableFuture() {
		return lastCompletableFuture;
	}

	public CompletableFuture<Void> complete() {
		collectAndJoinAsyncTasks(true);
		return lastCompletableFuture;
	}
	
	void collectAndJoinAsyncTasks(boolean isLast) {
		if (isLast && !isCompleting.compareAndSet(false, true)) {
			log.warnf("Already completing the cache %s", getName());
			return;
		}
		if (isLast || futures.size() > collectFutureThreshold) {
			if (!isCollectProcessing.compareAndSet(false, true)) {
				// Another thread is already collecting.
				return;
			}
			// Collect and join async tasks.
			log.debugf("Start collecting async tasks. futures.size = %s, isLast = %s", "" + futures.size(), isLast);

			while (futures.size() > collectFutureThreshold) {
				// Collect futures to be joined.
				Set<CompletableFuture<?>> setToJoin = futures.stream().filter(f -> {
					// If (k,v) is last entry, all futures must be waited.
					// Or else, process only isDone==true.
					return isLast || f.isDone();
				}).peek(futures::remove).collect(Collectors.toSet());
				// join collected futures and fire completionHandler, if any.
				log.debugf("Joining %d collected futures.", setToJoin.size());
				setToJoin.stream().forEach(CompletableFuture::join);
			}

			// Allow another collect processing.
			isCollectProcessing.set(false);

			if (isLast) {
				// Asynchronously, join all the rest of async task and call completionHandler, if any.
				lastCompletableFuture = CompletableFuture.runAsync(() -> {
					futures.stream().forEach(CompletableFuture::join);
					if (completionHandler != null) {
						log.infof("Calling completionHandler for cache %s", getName());
						completionHandler.run();
					}
				});
			}
		}
	}
	
	@Override
	public CompletableFuture<V> getAsync(K key) {
		log.tracef("### getAsync(%s) called.", key);
		CompletableFuture<V> future = delegate.getAsync(key);
		futures.add(future);

		boolean isLast = completionCondition.map(f -> f.test(key, null)).orElse(false);
		if (isLast)
			log.infof("This is the last operation on cache %s. key = %s", getName(), key);
		collectAndJoinAsyncTasks(isLast);

		return future;
	}

	@Override
	public CompletableFuture<Void> putAllAsync(Map<? extends K, ? extends V> arg0, long arg1, TimeUnit arg2, long arg3,
			TimeUnit arg4) {
		log.tracef("### putAllAsync(%s, %d, %s, %d, %s) called.", arg0, arg1, arg2, arg3, arg4);
		CompletableFuture<Void> future = delegate.putAllAsync(arg0, arg1, arg2, arg3, arg4);
		futures.add(future);

		boolean isLast = false;
		collectAndJoinAsyncTasks(isLast);

		return future;
	}

	@Override
	public CompletableFuture<Void> putAllAsync(Map<? extends K, ? extends V> arg0, long arg1, TimeUnit arg2) {
		log.tracef("### putAllAsync(%s, %d, %s) called.", arg0, arg1, arg2);
		CompletableFuture<Void> future = delegate.putAllAsync(arg0, arg1, arg2);
		futures.add(future);

		boolean isLast = false;
		collectAndJoinAsyncTasks(isLast);

		return future;
	}

	@Override
	public CompletableFuture<Void> putAllAsync(Map<? extends K, ? extends V> arg0) {
		log.tracef("### putAllAsync(%s) called.", arg0);
		CompletableFuture<Void> future = delegate.putAllAsync(arg0);
		futures.add(future);

		boolean isLast = false;
		collectAndJoinAsyncTasks(isLast);

		return future;
	}

	@Override
	public CompletableFuture<V> putAsync(K arg0, V arg1, long arg2, TimeUnit arg3, long arg4, TimeUnit arg5) {
		log.tracef("### putAsync(%s, %s, %d, %s, %d, %s) called.", arg0, arg1, arg2, arg3, arg4, arg5);
		CompletableFuture<V> future = delegate.putAsync(arg0, arg1, arg2, arg3, arg4, arg5);
		futures.add(future);

		boolean isLast = completionCondition.map(f -> f.test(arg0, arg1)).orElse(false);
		if (isLast)
			log.infof("This is the last operation on cache %s. key = %s, value = %s", getName(), arg0, arg1);
		collectAndJoinAsyncTasks(isLast);

		return future;
	}

	@Override
	public CompletableFuture<V> putAsync(K arg0, V arg1, long arg2, TimeUnit arg3) {
		log.tracef("### putAsync(%s, %s, %d, %s) called.", arg0, arg1, arg2, arg3);
		CompletableFuture<V> future = delegate.putAsync(arg0, arg1, arg2, arg3);
		futures.add(future);

		boolean isLast = completionCondition.map(f -> f.test(arg0, arg1)).orElse(false);
		if (isLast)
			log.infof("This is the last operation on cache %s. key = %s, value = %s", getName(), arg0, arg1);
		collectAndJoinAsyncTasks(isLast);

		return future;
	}

	@Override
	public CompletableFuture<V> putAsync(K key, V value) {
		log.tracef("### putAsync(%s, %s) called.", key, value);
		CompletableFuture<V> future = delegate.putAsync(key, value);
		futures.add(future);

		boolean isLast = completionCondition.map(f -> f.test(key, value)).orElse(false);
		if (isLast)
			log.infof("This is the last operation on cache %s. key = %s, value = %s", getName(), key, value);
		collectAndJoinAsyncTasks(isLast);

		return future;
	}

	@Override
	public CompletableFuture<V> putIfAbsentAsync(K arg0, V arg1, long arg2, TimeUnit arg3, long arg4, TimeUnit arg5) {
		log.tracef("### putIfAbsentAsync(%s, %s, %d, %s, %d, %s) called.", arg0, arg1, arg2, arg3, arg4, arg5);
		CompletableFuture<V> future = delegate.putIfAbsentAsync(arg0, arg1, arg2, arg3, arg4, arg5);
		futures.add(future);

		boolean isLast = completionCondition.map(f -> f.test(arg0, arg1)).orElse(false);
		if (isLast)
			log.infof("This is the last operation on cache %s. key = %s, value = %s", getName(), arg0, arg1);
		collectAndJoinAsyncTasks(isLast);

		return future;
	}

	@Override
	public CompletableFuture<V> putIfAbsentAsync(K arg0, V arg1, long arg2, TimeUnit arg3) {
		log.tracef("### putIfAbsentAsync(%s, %s, %d, %s) called.", arg0, arg1, arg2, arg3);
		CompletableFuture<V> future = delegate.putIfAbsentAsync(arg0, arg1, arg2, arg3);
		futures.add(future);

		boolean isLast = completionCondition.map(f -> f.test(arg0, arg1)).orElse(false);
		if (isLast)
			log.infof("This is the last operation on cache %s. key = %s, value = %s", getName(), arg0, arg1);
		collectAndJoinAsyncTasks(isLast);

		return future;
	}

	@Override
	public CompletableFuture<V> putIfAbsentAsync(K arg0, V arg1) {
		log.tracef("### putIfAbsentAsync(%s, %s) called.", arg0, arg1);
		CompletableFuture<V> future = delegate.putIfAbsentAsync(arg0, arg1);
		futures.add(future);

		boolean isLast = completionCondition.map(f -> f.test(arg0, arg1)).orElse(false);
		if (isLast)
			log.infof("This is the last operation on cache %s. key = %s, value = %s", getName(), arg0, arg1);
		collectAndJoinAsyncTasks(isLast);

		return future;
	}

	@Override
	public CompletableFuture<Boolean> removeAsync(Object arg0, Object arg1) {
		log.tracef("### removeAsync(%s, %s) called.", arg0, arg1);
		CompletableFuture<Boolean> future = delegate.removeAsync(arg0, arg1);
		futures.add(future);

		boolean isLast = completionCondition.map(f -> f.test((K)arg0, (V)arg1)).orElse(false);
		if (isLast)
			log.infof("This is the last operation on cache %s. key = %s, value = %s", getName(), arg0, arg1);
		collectAndJoinAsyncTasks(isLast);

		return future;
	}

	@Override
	public CompletableFuture<V> removeAsync(Object arg0) {
		log.tracef("### removeAsync(%s) called.", arg0);
		CompletableFuture<V> future = delegate.removeAsync(arg0);
		futures.add(future);

		boolean isLast = completionCondition.map(f -> f.test((K)arg0, null)).orElse(false);
		if (isLast)
			log.infof("This is the last operation on cache %s. key = %s", getName(), arg0);
		collectAndJoinAsyncTasks(isLast);

		return future;
	}

	@Override
	public CompletableFuture<Boolean> removeWithVersionAsync(K arg0, long arg1) {
		log.tracef("### removeWithVersionAsync(%s, %d) called.", arg0, arg1);
		CompletableFuture<Boolean> future = delegate.removeAsync(arg0, arg1);
		futures.add(future);

		boolean isLast = completionCondition.map(f -> f.test((K)arg0, null)).orElse(false);
		if (isLast)
			log.infof("This is the last operation on cache %s. key = %s", getName(), arg0);
		collectAndJoinAsyncTasks(isLast);

		return future;
	}

	@Override
	public CompletableFuture<V> replaceAsync(K arg0, V arg1, long arg2, TimeUnit arg3, long arg4, TimeUnit arg5) {
		log.tracef("### replaceAsync(%s, %s, %d, %s, %d, %s) called.", arg0, arg1, arg2, arg3, arg4, arg5);
		CompletableFuture<V> future = delegate.replaceAsync(arg0, arg1, arg2, arg3, arg4, arg5);
		futures.add(future);

		boolean isLast = completionCondition.map(f -> f.test(arg0, arg1)).orElse(false);
		if (isLast)
			log.infof("This is the last operation on cache %s. key = %s, value = %s", getName(), arg0, arg1);
		collectAndJoinAsyncTasks(isLast);

		return future;
	}

	@Override
	public CompletableFuture<V> replaceAsync(K arg0, V arg1, long arg2, TimeUnit arg3) {
		log.tracef("### replaceAsync(%s, %s, %d, %s) called.", arg0, arg1, arg2, arg3);
		CompletableFuture<V> future = delegate.replaceAsync(arg0, arg1, arg2, arg3);
		futures.add(future);

		boolean isLast = completionCondition.map(f -> f.test(arg0, arg1)).orElse(false);
		if (isLast)
			log.infof("This is the last operation on cache %s. key = %s, value = %s", getName(), arg0, arg1);
		collectAndJoinAsyncTasks(isLast);

		return future;
	}

	@Override
	public CompletableFuture<Boolean> replaceAsync(K arg0, V arg1, V arg2, long arg3, TimeUnit arg4, long arg5,
			TimeUnit arg6) {
		log.tracef("### replaceAsync(%s, %s, %s, %d, %s, %d, %s) called.", arg0, arg1, arg2, arg3, arg4, arg5, arg6);
		CompletableFuture<Boolean> future = delegate.replaceAsync(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
		futures.add(future);

		boolean isLast = completionCondition.map(f -> f.test(arg0, arg2)).orElse(false);
		if (isLast)
			log.infof("This is the last operation on cache %s. key = %s, value = %s", getName(), arg0, arg2);
		collectAndJoinAsyncTasks(isLast);

		return future;
	}

	@Override
	public CompletableFuture<Boolean> replaceAsync(K arg0, V arg1, V arg2, long arg3, TimeUnit arg4) {
		log.tracef("### replaceAsync(%s, %s, %s, %d, %s) called.", arg0, arg1, arg2, arg3, arg4);
		CompletableFuture<Boolean> future = delegate.replaceAsync(arg0, arg1, arg2, arg3, arg4);
		futures.add(future);

		boolean isLast = completionCondition.map(f -> f.test(arg0, arg2)).orElse(false);
		if (isLast)
			log.infof("This is the last operation on cache %s. key = %s, value = %s", getName(), arg0, arg2);
		collectAndJoinAsyncTasks(isLast);

		return future;
	}

	@Override
	public CompletableFuture<Boolean> replaceAsync(K arg0, V arg1, V arg2) {
		log.tracef("### replaceAsync(%s, %s, %s) called.", arg0, arg1, arg2);
		CompletableFuture<Boolean> future = delegate.replaceAsync(arg0, arg1, arg2);
		futures.add(future);

		boolean isLast = completionCondition.map(f -> f.test(arg0, arg2)).orElse(false);
		if (isLast)
			log.infof("This is the last operation on cache %s. key = %s, value = %s", getName(), arg0, arg2);
		collectAndJoinAsyncTasks(isLast);

		return future;
	}

	@Override
	public CompletableFuture<V> replaceAsync(K arg0, V arg1) {
		log.tracef("### replaceAsync(%s, %s) called.", arg0, arg1);
		CompletableFuture<V> future = delegate.replaceAsync(arg0, arg1);
		futures.add(future);

		boolean isLast = completionCondition.map(f -> f.test(arg0, arg1)).orElse(false);
		if (isLast)
			log.infof("This is the last operation on cache %s. key = %s, value = %s", getName(), arg0, arg1);
		collectAndJoinAsyncTasks(isLast);

		return future;
	}

	@Override
	public CompletableFuture<Boolean> replaceWithVersionAsync(K arg0, V arg1, long arg2, int arg3, int arg4) {
		log.tracef("### replaceWithVersionAsync(%s, %s, %d, %d, %d) called.", arg0, arg1, arg2, arg3, arg4);
		CompletableFuture<Boolean> future = delegate.replaceWithVersionAsync(arg0, arg1, arg2, arg3, arg4);
		futures.add(future);

		boolean isLast = completionCondition.map(f -> f.test(arg0, arg1)).orElse(false);
		if (isLast)
			log.infof("This is the last operation on cache %s. key = %s, value = %s", getName(), arg0, arg1);
		collectAndJoinAsyncTasks(isLast);

		return future;
	}

	@Override
	public CompletableFuture<Boolean> replaceWithVersionAsync(K arg0, V arg1, long arg2, int arg3) {
		log.tracef("### replaceWithVersionAsync(%s, %s, %d, %d) called.", arg0, arg1, arg2, arg3);
		CompletableFuture<Boolean> future = delegate.replaceWithVersionAsync(arg0, arg1, arg2, arg3);
		futures.add(future);

		boolean isLast = completionCondition.map(f -> f.test(arg0, arg1)).orElse(false);
		if (isLast)
			log.infof("This is the last operation on cache %s. key = %s, value = %s", getName(), arg0, arg1);
		collectAndJoinAsyncTasks(isLast);

		return future;
	}

	@Override
	public CompletableFuture<Boolean> replaceWithVersionAsync(K arg0, V arg1, long arg2) {
		log.tracef("### replaceWithVersionAsync(%s, %s, %d) called.", arg0, arg1, arg2);
		CompletableFuture<Boolean> future = delegate.replaceWithVersionAsync(arg0, arg1, arg2);
		futures.add(future);

		boolean isLast = completionCondition.map(f -> f.test(arg0, arg1)).orElse(false);
		if (isLast)
			log.infof("This is the last operation on cache %s. key = %s, value = %s", getName(), arg0, arg1);
		collectAndJoinAsyncTasks(isLast);

		return future;
	}

}
