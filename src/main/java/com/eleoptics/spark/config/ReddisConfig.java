package com.eleoptics.spark.config;

import com.eleoptics.spark.cache.AssetDAO;
import com.eleoptics.spark.cache.AssetDAOImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.cache.Cache;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ComponentScan
@EnableCaching
public class ReddisConfig {

    /*
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson(ObjectMapper objectMapper) throws IOException {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379");

        config.setCodec(new JsonJacksonCodec(objectMapper));
        return Redisson.create(config);
    }

    @Bean
    public CacheManager cacheManager(RedissonClient redissonClient) {
        Map<String, CacheConfig> config = new HashMap<String, CacheConfig>();

        // create "testMap" cache with ttl = 24 minutes and maxIdleTime = 12 minutes
        config.put("testMap", new CacheConfig(24 * 60 * 1000, 12 * 60 * 1000));
        return new RedissonSpringCacheManager(redissonClient, config);
    }

    @Bean
    public AssetDAO assetDAO(RedissonClient redissonClient){
        return new AssetDAOImpl(redissonClient);
    }

    @Bean
    public ObjectMapper objectMapper(){
        // with 3.0 (or with 2.10 as alternative)
        ObjectMapper mapper = JsonMapper.builder() // or different mapper for other format
                .addModule(new ParameterNamesModule())
                .addModule(new Jdk8Module())
                .addModule(new JavaTimeModule())
                // and possibly other configuration, modules, then:
                .build();

        return mapper;
    }

     */
}


