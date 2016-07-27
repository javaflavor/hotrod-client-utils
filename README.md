# hotrod-client-utils

This module contains some utility classes for Hot Rod clients.

* ParallelRemoteCache
* PartitionedCacheSelector

## Prerequisite

* Red Hat JBoss Data Grid 7.0.0 Server

The class PartitionedCacheSelector requires server task modules, manage-cache-task and list-cache-task.
After starting JDG 7 servers, you must build the server task modules and deploy these on all JDG server of the target cluster.

~~~
$ cd hotrod-server-tasks; maven clean package
$ ${JDG_HOME}/bin/ispn-cli.sh -c --controller=localhost:9990
[] deploy manage-cache-task/target/manage-cache-task.jar
[] deploy list-cache-task/target/list-cache-task.jar
~~~

The class ParallelRemoteCache does not require any server tasks.

## ParallelRemoteCache

In general, JDG server will get best performace with lots of concurrent accesses, usually hundreds of dedicated client threads. You can use asynchronous version of RemoteCache operations like putAsync() for put(), this will generate multi-threaded access to the JDG server. But if the input rate is faster than the processing capacity of the JDG server, the thread pool and the queue of client-side executor will easily overflow and cause java.util.concurrent.RejectedExecutionException or java.net.ConnectException: Connection refused.

ParallelRemoteCache will provide the following features:

* Automatic input throttling around the pool size of asynchronous executor. This will avoid java.util.concurrent.RejectedExecutionException thrown.
* Registering completion handler and completion condition. If the completion condition is satisfied, automatically execute an arbitrary logic represented by lambda expression.

ParallelRemoteCache is initialized as follows:

~~~
// Create ParallelRemoteCache from original RemoteCache.
ParallelRemoteCache<Integer,String> cache = new ParallelRemoteCacheBuilder<Integer,String>()
		.cache(origCache)
		.completionCondition(
				(k,v) -> k == 1000
		)
		.completionHandler(
				(c) -> System.out.println("$$$ All entries are stored with 'isLastEntry rule.")
		)
		.build();

// Use async version of operations.	
IntStream.range(1, 1001)
	.forEach(i -> cache.putAsync(i, "val_"+String.format("%03d", i)));
~~~

## PartitionedCacheSelector

PartitionedCacheSelector is a utility class for inserting a lot of data into partitioned cache. The partitioned cache is automatically created if necessary.

PartitionedCacheSelector also automatically calls arbitrary logic, partitionCompletionHandler, when each partition completed. The partitionCompletionHandler is called asynchronously. Therefore, the input operations are not blocked.

PartitionedCacheSelector is initialized as follows:

~~~
RemoteCacheManager manager = new RemoteCacheManager();
int chunkSize = 1000;

// Create a PartitionedCacheSelector.
PartitionedCacheSelector<Integer,String> selector = new PartitionedCacheSelectorBuilder<Integer,String>()
		.baseCacheName("partitioned")
		.cacheManager(manager)
		.partitionIdFunction(
				(k,v) -> Integer.toString((k-1) / chunkSize)	// partitionId
		)
		.partitionCompletionPredicate(
				(k,v) -> k%chunkSize == 0						// end partition rule
		)
		.partitionCompletionHandler(
				(c) -> System.out.println("Partition data is uploaded.")	// end partition handler
		)
		.build();

// Use async version of operations.	
IntStream.range(1, 10001)
	.mapToObj(i -> new Tuple<Integer,String>(i, "val_"+String.format("%03d", i)))
	.forEach(e -> 
		// Select or create the appropriate partitioned cache.
		selector.getPartitionedCache(e.key, e.value)
			.putAsync(e.key, e.value)
	);
~~~
