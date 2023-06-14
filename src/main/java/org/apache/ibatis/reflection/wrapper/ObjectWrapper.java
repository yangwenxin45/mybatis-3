/**
 *    Copyright 2009-2023 the original author or authors.
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
package org.apache.ibatis.reflection.wrapper;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyTokenizer;

import java.util.List;

/**
 * @author Clinton Begin
 */
public interface ObjectWrapper {

  /**
   * 获得被包装对象某个属性的值
   *
   * @author yangwenxin
   * @date 2023-03-01 15:03
   */
  Object get(PropertyTokenizer prop);

  /**
   * 设置被包装对象某个属性的值
   *
   * @author yangwenxin
   * @date 2023-03-01 15:04
   */
  void set(PropertyTokenizer prop, Object value);

  /**
   * 找到对应的属性名称
   *
   * @author yangwenxin
   * @date 2023-03-01 15:04
   */
  String findProperty(String name, boolean useCamelCaseMapping);

  /**
   * 获得所有的属性get方法名称
   *
   * @author yangwenxin
   * @date 2023-03-01 15:04
   */
  String[] getGetterNames();

  /**
   * 获得所有属性的set方法名称
   *
   * @author yangwenxin
   * @date 2023-03-01 15:05
   */
  String[] getSetterNames();

  /**
   * 获得指定属性的set方法的类型
   *
   * @author yangwenxin
   * @date 2023-03-01 15:05
   */
  Class<?> getSetterType(String name);

  /**
   * 获取指定属性的get方法的类型
   *
   * @author yangwenxin
   * @date 2023-03-01 15:06
   */
  Class<?> getGetterType(String name);

  /**
   * 判断某个属性是否有对应的set方法
   *
   * @author yangwenxin
   * @date 2023-03-01 15:06
   */
  boolean hasSetter(String name);

  /**
   * 判断某个属性是否有对应的get方法
   *
   * @author yangwenxin
   * @date 2023-03-01 15:07
   */
  boolean hasGetter(String name);

  /**
   * 实例化某个属性的值
   *
   * @author yangwenxin
   * @date 2023-03-01 15:07
   */
  MetaObject instantiatePropertyValue(String name, PropertyTokenizer prop, ObjectFactory objectFactory);

  boolean isCollection();

  void add(Object element);

  <E> void addAll(List<E> element);

}
