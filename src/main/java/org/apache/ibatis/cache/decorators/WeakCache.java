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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Deque;
import java.util.LinkedList;

/**
 * Weak Reference cache decorator.
 * Thanks to Dr. Heinz Kabutz for his guidance here.
 * 通过将缓存数据包装成弱引用的数据，从而使得JVM可以清理掉缓存数据
 * 缓存中存储的数据是"数据键：<数据值>"的形式，
 * 而经过WeakCache包装后，缓存中存储的数据是"数据键：弱引用包装<数据值>"的形式，
 * 当弱引用的数据被JVM回收后，缓存中的数据会变成"数据键：弱引用包装<null>"的形式
 *
 * @author Clinton Begin
 */
public class WeakCache implements Cache {
  // 强引用的对象列表
  private final Deque<Object> hardLinksToAvoidGarbageCollection;
  // 弱引用的对象列表
  private final ReferenceQueue<Object> queueOfGarbageCollectedEntries;
  // 被装饰对象
  private final Cache delegate;
  // 强引用对象的数目限制
  private int numberOfHardLinks;

  public WeakCache(Cache delegate) {
    this.delegate = delegate;
    this.numberOfHardLinks = 256;
    this.hardLinksToAvoidGarbageCollection = new LinkedList<>();
    this.queueOfGarbageCollectedEntries = new ReferenceQueue<>();
  }

  @Override
  public String getId() {
    return delegate.getId();
  }

  @Override
  public int getSize() {
    removeGarbageCollectedItems();
    return delegate.getSize();
  }

  public void setSize(int size) {
    this.numberOfHardLinks = size;
  }

  @Override
  public void putObject(Object key, Object value) {
    // 清理垃圾回收队列中的元素
    removeGarbageCollectedItems();
    // 向被装饰对象中放入的值是弱引用的句柄
    delegate.putObject(key, new WeakEntry(key, value, queueOfGarbageCollectedEntries));
  }

  @Override
  public Object getObject(Object key) {
    Object result = null;
    // 假定被装饰对象只被该装饰器完全控制
    @SuppressWarnings("unchecked") // assumed delegate cache is totally managed by this cache
    WeakReference<Object> weakReference = (WeakReference<Object>) delegate.getObject(key);
    if (weakReference != null) {
      result = weakReference.get();
      // 弱引用对象已经被清理
      if (result == null) {
        // 直接删除该缓存
        delegate.removeObject(key);
      } else {
        // 弱引用对象还存在，将缓存中的数据写入强引用列表中，防止其被清理
        hardLinksToAvoidGarbageCollection.addFirst(result);
        // 强引用的对象数目超出限制
        if (hardLinksToAvoidGarbageCollection.size() > numberOfHardLinks) {
          // 从强引用的列表中删除该数据
          hardLinksToAvoidGarbageCollection.removeLast();
        }
      }
    }
    return result;
  }

  @Override
  public Object removeObject(Object key) {
    removeGarbageCollectedItems();
    return delegate.removeObject(key);
  }

  @Override
  public void clear() {
    hardLinksToAvoidGarbageCollection.clear();
    removeGarbageCollectedItems();
    delegate.clear();
  }

  /**
   * 将值已经被JVM清理掉的缓存数据从缓存中删除
   *
   * @author yangwenxin
   * @date 2023-06-07 14:28
   */
  private void removeGarbageCollectedItems() {
    WeakEntry sv;
    // 轮询该垃圾回收队列
    while ((sv = (WeakEntry) queueOfGarbageCollectedEntries.poll()) != null) {
      // 将该队列中涉及的键删除
      delegate.removeObject(sv.key);
    }
  }

  private static class WeakEntry extends WeakReference<Object> {
    // 该变量不会被JVM清理掉，这里存储了目标对象的键
    private final Object key;

    private WeakEntry(Object key, Object value, ReferenceQueue<Object> garbageCollectionQueue) {
      super(value, garbageCollectionQueue);
      this.key = key;
    }
  }

}
