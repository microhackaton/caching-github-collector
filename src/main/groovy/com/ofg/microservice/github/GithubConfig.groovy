package com.ofg.microservice.github

import groovy.transform.TypeChecked
import net.sf.ehcache.config.CacheConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.cache.ehcache.EhCacheCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.social.twitter.api.Twitter
import org.springframework.social.twitter.api.impl.TwitterTemplate

@Configuration
@TypeChecked
class GithubConfig {

    public static final String GITHUB_URL = "https://api.github.com/"

//    @Bean
//    @Autowired
//    Twitter twitter(@Value('${twitter.id}') String id, @Value('${twitter.secret}') String secret) {
//        return new TwitterTemplate(id, secret)
//    }

    @Bean
    CacheManager cacheManager() {
        CacheConfiguration cacheConfiguration = new CacheConfiguration()
        cacheConfiguration.setName("github")
        cacheConfiguration.setMemoryStoreEvictionPolicy("LRU")
        cacheConfiguration.setMaxEntriesLocalHeap(10000)
        cacheConfiguration.setTimeToLiveSeconds(60 * 10)
        net.sf.ehcache.config.Configuration config = new net.sf.ehcache.config.Configuration()
        config.addCache(cacheConfiguration)
        return new EhCacheCacheManager( net.sf.ehcache.CacheManager.newInstance(config) )
    }
}