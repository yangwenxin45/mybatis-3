/**
 *    Copyright 2009-2016 the original author or authors.
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
package org.apache.ibatis.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * References a generic type.
 *
 * @param <T> the referenced type
 * @author Simone Tripodi
 * @since 3.1.0
 */
public abstract class TypeReference<T> {

  /**
   * 泛型参数T的实际类型
   *
   * @author yangwenxin
   * @date 2023-03-09 10:57
   */
  private final Type rawType;

  protected TypeReference() {
    // 返回对象的运行时类类型，也就是对象的实现类类型
    rawType = getSuperclassTypeParameter(getClass());
  }

  /**
   * 解析出当前TypeHandler实现类能够处理的目标类型
   *
   * @author yangwenxin
   * @date 2023-03-09 10:26
   */
  Type getSuperclassTypeParameter(Class<?> clazz) {
    Type genericSuperclass = clazz.getGenericSuperclass();
    if (genericSuperclass instanceof Class) {
      // try to climb up the hierarchy until meet something useful
      if (TypeReference.class != genericSuperclass) {
        // 说明没有解析到足够上层，将clazz类的父类作为输入参数递归调用
        return getSuperclassTypeParameter(clazz.getSuperclass());
      }

      // 说明clazz实现了TypeReference类，但是却没有使用泛型
      throw new TypeException("'" + getClass() + "' extends TypeReference but misses the type parameter. "
        + "Remove the extension or add a type parameter to it.");
    }

    // 运行到这里说明genericSuperclass是泛型类，获取泛型类的第一个参数
    Type rawType = ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
    // TODO remove this when Reflector is fixed to return Types
    if (rawType instanceof ParameterizedType) {
      rawType = ((ParameterizedType) rawType).getRawType();
    }

    return rawType;
  }

  public final Type getRawType() {
    return rawType;
  }

  @Override
  public String toString() {
    return rawType.toString();
  }

}
