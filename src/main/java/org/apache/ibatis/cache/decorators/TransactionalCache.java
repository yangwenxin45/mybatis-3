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
package org.apache.ibatis.cache.decorators;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The 2nd level cache transactional buffer.
 * <p>
 * This class holds all cache entries that are to be added to the 2nd level cache during a Session.
 * Entries are sent to the cache when commit is called or discarded if the Session is rolled back.
 * Blocking cache support has been added. Therefore any get() that returns a cache miss
 * will be followed by a put() so any lock associated with the key can be released.
 *
 * @author Clinton Begin
 * @author Eduardo Macarron
 */
// 事务装饰器：用于支持事务操作的装饰器
public class TransactionalCache implements Cache {

  private static final Log log = LogFactory.getLog(TransactionalCache.class);

  // 被装饰的对象
  private final Cache delegate;
  // 事务提交后是否直接清理缓存
  private boolean clearOnCommit;
  // 事务提交时需要写入缓存的数据
  private final Map<Object, Object> entriesToAddOnCommit;
  // 缓存查询未命中的数据
  /**
   * entriesMissedInCache作用：需要结合阻塞装饰器BlockingCache思考
   * 事务缓存中使用的缓存可能是被BlockingCache装饰过的，这意味着，如果缓存查询得到的结果为null，会导致对该数据上锁，从而阻塞后续对该数据的查询
   * 而事务提交或者回滚后，应该对缓存中的这些数据全部解锁才对
   * entriesMissedInCache就保存了这些键，在事务结束时对这些数据进行解锁
   */
  private final Set<Object> entriesMissedInCache;

  public TransactionalCache(Cache delegate) {
    this.delegate = delegate;
    this.clearOnCommit = false;
    this.entriesToAddOnCommit = new HashMap<>();
    this.entriesMissedInCache = new HashSet<>();
  }

  @Override
  public String getId() {
    return delegate.getId();
  }

  @Override
  public int getSize() {
    return delegate.getSize();
  }

  @Override
  public Object getObject(Object key) {
    // issue #116
    Object object = delegate.getObject(key);
    // 缓存未命中
    if (object == null) {
      // 记录该缓存未命中
      entriesMissedInCache.add(key);
    }
    // issue #146
    // 如果设置了提交时立马清除，则直接返回null
    if (clearOnCommit) {
      return null;
    } else {
      return object;
    }
  }

  @Override
  public void putObject(Object key, Object object) {
    // 先放到entriesToAddOnCommit列表中暂存
    entriesToAddOnCommit.put(key, object);
  }

  @Override
  public Object removeObject(Object key) {
    return null;
  }

  @Override
  public void clear() {
    clearOnCommit = true;
    entriesToAddOnCommit.clear();
  }

  public void commit() {
    if (clearOnCommit) {
      delegate.clear();
    }
    // 将未写入缓存的操作写入缓存
    flushPendingEntries();
    // 清理环境
    reset();
  }

  public void rollback() {
    unlockMissedEntries();
    reset();
  }

  /**
   * 清理环境
   *
   * @author yangwenxin
   * @date 2023-06-08 09:47
   */
  private void reset() {
    clearOnCommit = false;
    entriesToAddOnCommit.clear();
    entriesMissedInCache.clear();
  }

  /**
   * 将未写入缓存的操作写入缓存
   *
   * @author yangwenxin
   * @date 2023-06-08 09:47
   */
  private void flushPendingEntries() {
    // 将entriesToAddOnCommit中的数据写入缓存
    for (Map.Entry<Object, Object> entry : entriesToAddOnCommit.entrySet()) {
      delegate.putObject(entry.getKey(), entry.getValue());
    }
    // 将entriesMissedInCache中的数据写入缓存
    for (Object entry : entriesMissedInCache) {
      if (!entriesToAddOnCommit.containsKey(entry)) {
        delegate.putObject(entry, null);
      }
    }
  }

  /**
   * 删除缓存未命中的数据
   *
   * @author yangwenxin
   * @date 2023-06-08 09:50
   */
  private void unlockMissedEntries() {
    for (Object entry : entriesMissedInCache) {
      try {
        delegate.removeObject(entry);
      } catch (Exception e) {
        log.warn("Unexpected exception while notifiying a rollback to the cache adapter."
          + "Consider upgrading your cache adapter to the latest version.  Cause: " + e);
      }
    }
  }

}
