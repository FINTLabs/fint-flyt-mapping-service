package no.fintlabs.cache.ehcache

import no.fintlabs.cache.FintCacheManager
import no.fintlabs.cache.FintCacheManagerSpec
import no.fintlabs.cache.FintCacheOptions

class FintEhCacheManagerSpec extends FintCacheManagerSpec {

    @Override
    protected FintCacheManager createCacheManager(FintCacheOptions defaultCacheOptions) {
        return new FintEhCacheManager(defaultCacheOptions)
    }

    def 'should be instance of FintEhCacheManager'() {
        expect:
        fintCacheManager instanceof FintEhCacheManager
    }

    def 'should create FintEhCache'() {
        expect:
        fintCacheManager.createCache('testCache', String.class, Integer.class) instanceof FintEhCache
    }

}
