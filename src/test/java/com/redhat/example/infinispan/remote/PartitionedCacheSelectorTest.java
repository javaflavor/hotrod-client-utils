package com.redhat.example.infinispan.remote;

import java.util.stream.IntStream;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.server.hotrod.HotRodServer;
import org.infinispan.server.hotrod.configuration.HotRodServerConfiguration;
import org.infinispan.server.hotrod.configuration.HotRodServerConfigurationBuilder;
import org.infinispan.server.infinispan.task.ServerTaskRegistry;
import org.infinispan.server.infinispan.task.ServerTaskRegistryImpl;
import org.jboss.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.redhat.example.ListCacheTask;
import com.redhat.example.ManageCacheTask;

public class PartitionedCacheSelectorTest {
	static Logger log = Logger.getLogger(PartitionedCacheSelectorTest.class);
	
	int port = 11233;
	HotRodServer server;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		GlobalConfiguration gconf = new GlobalConfigurationBuilder()
				.nonClusteredDefault()
				.globalJmxStatistics().allowDuplicateDomains(true)
				.build();
		org.infinispan.configuration.cache.Configuration cconf =
				new org.infinispan.configuration.cache.ConfigurationBuilder().clustering().cacheMode(CacheMode.LOCAL).build();
		EmbeddedCacheManager manager = new DefaultCacheManager(gconf, cconf);
		HotRodServerConfiguration serverconf = new HotRodServerConfigurationBuilder().port(port).build();
		// Start Hot Rod Server.
		server = new HotRodServer();
		server.start(serverconf, manager);
		
		// Register the ServerTaskRegistry with required tasks.
		ServerTaskRegistry taskRegistry = new ServerTaskRegistryImpl();
		taskRegistry.addDeployedTask(new ManageCacheTask());
		taskRegistry.addDeployedTask(new ListCacheTask());
		manager.getGlobalComponentRegistry().registerComponent(taskRegistry, ServerTaskRegistry.class);
	}

	@After
	public void tearDown() throws Exception {
		if (server != null) server.stop();
	}

	@Test
	public void testGetPartitionedCache() {
		Configuration conf = new ConfigurationBuilder().addServers("localhost:"+port).build();
		RemoteCacheManager manager = new RemoteCacheManager(conf);
		int chunkSize = 1000;
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
						() -> System.out.println("Partition data is uploaded.")	// end partition handler
				)
				.build();

		log.info("Start load test.");
		
		IntStream.range(1, 10001)
			.mapToObj(i -> new Tuple<Integer,String>(i, "val_"+String.format("%03d", i)))
			.forEach(e -> 
				selector.getPartitionedCache(e.key, e.value)
					.putAsync(e.key, e.value)
			);

		log.info("End load test.");
		selector.getLastPartitionedCacheFuture().join();
		manager.stop();
	}
	
	static class Tuple<K,V> {
		K key;
		V value;
		public Tuple(K key, V value) {
			this.key = key;
			this.value = value;
		}
	}

}
