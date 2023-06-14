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
package org.apache.ibatis.scripting.xmltags;

import org.apache.ibatis.builder.BuilderException;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 对OGNL工具的进一步封装
 * 基于OGNL封装的表达式求值器是SQL节点树解析的利器，它能够根据上下文环境对表达式的值作出正确的判断
 *
 * @author Clinton Begin
 */
public class ExpressionEvaluator {

  /**
   * 对结果为true、false形式的表达式进行求值
   *
   * @param expression      表达式
   * @param parameterObject 参数对象
   * @return 求值结果
   */
  public boolean evaluateBoolean(String expression, Object parameterObject) {
    // 获取表达式的值
    Object value = OgnlCache.getValue(expression, parameterObject);
    // 如果确实是Boolean形式的结果
    if (value instanceof Boolean) {
      return (Boolean) value;
    }
    // 如果是数值形式的结果
    if (value instanceof Number) {
      return new BigDecimal(String.valueOf(value)).compareTo(BigDecimal.ZERO) != 0;
    }
    return value != null;
  }

  /**
   * 对结果为迭代形式的表达式进行求值
   *
   * @author yangwenxin
   * @date 2023-06-05 16:39
   */
  public Iterable<?> evaluateIterable(String expression, Object parameterObject) {
    // 获取表达式的结果
    Object value = OgnlCache.getValue(expression, parameterObject);
    if (value == null) {
      throw new BuilderException("The expression '" + expression + "' evaluated to a null value.");
    }
    // 如果结果是Iterable
    if (value instanceof Iterable) {
      return (Iterable<?>) value;
    }
    // 如果结果是Array
    if (value.getClass().isArray()) {
      // the array may be primitive, so Arrays.asList() may throw
      // a ClassCastException (issue 209).  Do the work manually
      // Curse primitives! :) (JGB)
      // 得到的Array可能是原始的，因此调用Arrays.asList()可能会抛出ClassCastException。所以需要手工转为ArrayList
      int size = Array.getLength(value);
      List<Object> answer = new ArrayList<>();
      for (int i = 0; i < size; i++) {
        Object o = Array.get(value, i);
        answer.add(o);
      }
      return answer;
    }
    // 如果结果是Map
    if (value instanceof Map) {
      return ((Map) value).entrySet();
    }
    throw new BuilderException("Error evaluating expression '" + expression + "'.  Return value (" + value + ") was not iterable.");
  }

}
