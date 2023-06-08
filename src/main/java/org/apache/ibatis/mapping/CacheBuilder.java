/**
 *    Copyright 2009-2019 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.mapping;

import org.apache.ibatis.builder.InitializingObject;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheException;
import org.apache.ibatis.cache.decorators.*;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 缓存建造者，负责完成缓存对象的创建
 * 组件缓存的过程就是根据需求为缓存的基本实现增加各种装饰的过程
 *
 * @author Clinton Begin
 */
public class CacheBuilder {
  private final String id;
  private Class<? extends Cache> implementation;
  private final List<Class<? extends Cache>> decorators;
  private Integer size;
  private Long clearInterval;
  private boolean readWrite;
  private Properties properties;
  private boolean blocking;

  public CacheBuilder(String id) {
    this.id = id;
    this.decorators = new ArrayList<>();
  }

  public CacheBuilder implementation(Class<? extends Cache> implementation) {
    this.implementation = implementation;
    return this;
  }

  public CacheBuilder addDecorator(Class<? extends Cache> decorator) {
    if (decorator != null) {
      this.decorators.add(decorator);
    }
    return this;
  }

  public CacheBuilder size(Integer size) {
    this.size = size;
    return this;
  }

  public CacheBuilder clearInterval(Long clearInterval) {
    this.clearInterval = clearInterval;
    return this;
  }

  public CacheBuilder readWrite(boolean readWrite) {
    this.readWrite = readWrite;
    return this;
  }

  public CacheBuilder blocking(boolean blocking) {
    this.blocking = blocking;
    return this;
  }

  public CacheBuilder properties(Properties properties) {
    this.properties = properties;
    return this;
  }

  /**
   * 组件缓存
   *
   * @author yangwenxin
   * @date 2023-06-07 15:11
   */
  public Cache build() {
    // 设置缓存的默认实现，默认装饰器
    setDefaultImplementations();
    // 创建默认的缓存
    Cache cache = newBaseCacheInstance(implementation, id);
    // 设置缓存的属性
    setCacheProperties(cache);
    // issue #352, do not apply decorators to custom caches
    // 缓存的实现类是PerpetualCache，不是用户自定义的缓存实现类
    if (PerpetualCache.class.equals(cache.getClass())) {
      // 为缓存逐级嵌套自定义的装饰器
      for (Class<? extends Cache> decorator : decorators) {
        // 应用用户自定义的缓存装饰器
        cache = newCacheDecoratorInstance(decorator, cache);
        // 为装饰器设置属性
        setCacheProperties(cache);
      }
      // 为缓存增加标准的装饰器
      cache = setStandardDecorators(cache);
    } else if (!LoggingCache.class.isAssignableFrom(cache.getClass())) {
      // 增加日志装饰器
      cache = new LoggingCache(cache);
    }
    // 返回被包装好的缓存
    return cache;
  }

  /**
   * 设置缓存的默认实现类和默认装饰器
   *
   * @author yangwenxin
   * @date 2023-06-07 15:11
   */
  private void setDefaultImplementations() {
    if (implementation == null) {
      implementation = PerpetualCache.class;
      if (decorators.isEmpty()) {
        decorators.add(LruCache.class);
      }
    }
  }

  /**
   * 为缓存增加标准的装饰器
   *
   * @author yangwenxin
   * @date 2023-06-08 09:33
   */
  private Cache setStandardDecorators(Cache cache) {
    try {
      MetaObject metaCache = SystemMetaObject.forObject(cache);
      // 设置缓存大小
      if (size != null && metaCache.hasSetter("size")) {
        metaCache.setValue("size", size);
      }
      // 如果定义了清理间隔，则使用定时清理装饰器装饰缓存
      if (clearInterval != null) {
        cache = new ScheduledCache(cache);
        ((ScheduledCache) cache).setClearInterval(clearInterval);
      }
      // 如果允许读写，则使用序列化装饰器装饰缓存
      if (readWrite) {
        cache = new SerializedCache(cache);
      }
      // 使用日志装饰器装饰缓存
      cache = new LoggingCache(cache);
      // 使用同步装饰器装饰缓存
      cache = new SynchronizedCache(cache);
      // 如果启用了阻塞功能，则使用阻塞装饰器装饰缓存
      if (blocking) {
        cache = new BlockingCache(cache);
      }
      // 返回被层层装饰的缓存
      return cache;
    } catch (Exception e) {
      throw new CacheException("Error building standard cache decorators.  Cause: " + e, e);
    }
  }

  private void setCacheProperties(Cache cache) {
    if (properties != null) {
      MetaObject metaCache = SystemMetaObject.forObject(cache);
      for (Map.Entry<Object, Object> entry : properties.entrySet()) {
        String name = (String) entry.getKey();
        String value = (String) entry.getValue();
        if (metaCache.hasSetter(name)) {
          Class<?> type = metaCache.getSetterType(name);
          if (String.class == type) {
            metaCache.setValue(name, value);
          } else if (int.class == type
              || Integer.class == type) {
            metaCache.setValue(name, Integer.valueOf(value));
          } else if (long.class == type
              || Long.class == type) {
            metaCache.setValue(name, Long.valueOf(value));
          } else if (short.class == type
              || Short.class == type) {
            metaCache.setValue(name, Short.valueOf(value));
          } else if (byte.class == type
              || Byte.class == type) {
            metaCache.setValue(name, Byte.valueOf(value));
          } else if (float.class == type
              || Float.class == type) {
            metaCache.setValue(name, Float.valueOf(value));
          } else if (boolean.class == type
              || Boolean.class == type) {
            metaCache.setValue(name, Boolean.valueOf(value));
          } else if (double.class == type
              || Double.class == type) {
            metaCache.setValue(name, Double.valueOf(value));
          } else {
            throw new CacheException("Unsupported property type for cache: '" + name + "' of type " + type);
          }
        }
      }
    }
    if (InitializingObject.class.isAssignableFrom(cache.getClass())) {
      try {
        ((InitializingObject) cache).initialize();
      } catch (Exception e) {
        throw new CacheException("Failed cache initialization for '"
          + cache.getId() + "' on '" + cache.getClass().getName() + "'", e);
      }
    }
  }

  private Cache newBaseCacheInstance(Class<? extends Cache> cacheClass, String id) {
    Constructor<? extends Cache> cacheConstructor = getBaseCacheConstructor(cacheClass);
    try {
      return cacheConstructor.newInstance(id);
    } catch (Exception e) {
      throw new CacheException("Could not instantiate cache implementation (" + cacheClass + "). Cause: " + e, e);
    }
  }

  private Constructor<? extends Cache> getBaseCacheConstructor(Class<? extends Cache> cacheClass) {
    try {
      return cacheClass.getConstructor(String.class);
    } catch (Exception e) {
      throw new CacheException("Invalid base cache implementation (" + cacheClass + ").  "
        + "Base cache implementations must have a constructor that takes a String id as a parameter.  Cause: " + e, e);
    }
  }

  private Cache newCacheDecoratorInstance(Class<? extends Cache> cacheClass, Cache base) {
    Constructor<? extends Cache> cacheConstructor = getCacheDecoratorConstructor(cacheClass);
    try {
      return cacheConstructor.newInstance(base);
    } catch (Exception e) {
      throw new CacheException("Could not instantiate cache decorator (" + cacheClass + "). Cause: " + e, e);
    }
  }

  private Constructor<? extends Cache> getCacheDecoratorConstructor(Class<? extends Cache> cacheClass) {
    try {
      return cacheClass.getConstructor(Cache.class);
    } catch (Exception e) {
      throw new CacheException("Invalid cache decorator (" + cacheClass + ").  "
        + "Cache decorators must have a constructor that takes a Cache instance as a parameter.  Cause: " + e, e);
    }
  }
}
