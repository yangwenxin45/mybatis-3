/**
 *    Copyright 2009-2017 the original author or authors.
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

/**
 * 对应数据库操作节点中的if节点。通过if节点可以让Mybatis根据参数等信息决定是否写入一段SQL片段
 *
 * @author Clinton Begin
 */
public class IfSqlNode implements SqlNode {
  // 表达式求值器
  private final ExpressionEvaluator evaluator;
  // if判断时的测试条件
  private final String test;
  // 如果if成立，要被拼接的SQL片段信息
  private final SqlNode contents;

  public IfSqlNode(SqlNode contents, String test) {
    this.test = test;
    this.contents = contents;
    this.evaluator = new ExpressionEvaluator();
  }

  @Override
  public boolean apply(DynamicContext context) {
    // 判断if条件是否成立
    if (evaluator.evaluateBoolean(test, context.getBindings())) {
      // 将contents拼接到context
      contents.apply(context);
      return true;
    }
    return false;
  }

}
