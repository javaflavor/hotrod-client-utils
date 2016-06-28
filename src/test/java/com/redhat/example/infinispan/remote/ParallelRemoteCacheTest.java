package com.redhat.example.infinispan.remote;

import java.util.stream.IntStream;

import org.infinispan.client.hotrod.RemoteCache;
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
import org.jboss.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ParallelRemoteCacheTest {
	static Logger log = Logger.getLogger(ParallelRemoteCacheTest.class);

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
	}

	@After
	public void tearDown() throws Exception {
		server.stop();
	}

	@Test
	public void testPutAsyncKV_with_auto_complete() {
		Configuration conf = new ConfigurationBuilder().addServers("localhost:"+port).build();
		RemoteCacheManager manager = new RemoteCacheManager(conf);
		RemoteCache<Integer,String> origCache = manager.getCache();
		ParallelRemoteCache<Integer,String> cache = new ParallelRemoteCacheBuilder<Integer,String>()
				.cache(origCache)
				.completionCondition(
						(k,v) -> k == 1000
				)
				.completionHandler(
						(c) -> System.out.println("$$$ All entries are stored with 'isLastEntry rule'. cache: "+c.getName())
				)
				.build();
				
		log.info("Start load test.");
		
		IntStream.range(1, 1001)
			.forEach(i -> cache.putAsync(i, "val_"+String.format("%03d", i)));

		log.info("End load test.");
		cache.getLastCompletableFuture().join();
		manager.stop();
	}

	@Test
	public void testPutAsyncKV_with_complete() {
		Configuration conf = new ConfigurationBuilder().addServers("localhost:"+port).build();
		RemoteCacheManager manager = new RemoteCacheManager(conf);
		RemoteCache<Integer,String> origCache = manager.getCache();
		ParallelRemoteCache<Integer,String> cache = new ParallelRemoteCacheBuilder<Integer,String>()
				.cache(origCache)
				.completionHandler(
						(c) -> System.out.println("$$$ All entries are stored with complete(). cache: "+c.getName())
				)
				.build();

		log.info("Start load test.");
		
		IntStream.range(1, 1001)
			.forEach(i -> cache.putAsync(i, "val_"+String.format("%03d", i)));
		log.info("End load test."); 
		cache.complete().join();
		manager.stop();
	}

	@Test
	public void testPutAsyncKV_with_auto_duplicated_complete() {
		Configuration conf = new ConfigurationBuilder().addServers("localhost:"+port).build();
		RemoteCacheManager manager = new RemoteCacheManager(conf);
		RemoteCache<Integer,String> origCache = manager.getCache();
		ParallelRemoteCache<Integer,String> cache = new ParallelRemoteCacheBuilder<Integer,String>()
				.cache(origCache)
				.completionCondition(
						(k,v) -> k == 1000
				)
				.completionHandler(
						(c) -> System.out.println("$$$ All entries are stored with 'isLastEntry rule'. cache: "+c.getName())
				)
				.build();

		log.info("Start load test.");
		
		IntStream.range(1, 1001)
			.forEach(i -> cache.putAsync(i, "val_"+String.format("%03d", i)));

		// If already completing, invoking complete() will be ignored.
		cache.complete();
		
		log.info("End load test.");
		cache.getLastCompletableFuture().join();
		manager.stop();
	}

}
