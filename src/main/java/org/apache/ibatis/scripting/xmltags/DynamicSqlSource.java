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
package org.apache.ibatis.scripting.xmltags;

import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;

/**
 * 动态SQL语句
 * 所谓动态SQL是指含有动态SQL节点（如"if"节点）或者含有"${}"占位符
 *
 * @author Clinton Begin
 */
public class DynamicSqlSource implements SqlSource {

  private final Configuration configuration;
  private final SqlNode rootSqlNode;

  public DynamicSqlSource(Configuration configuration, SqlNode rootSqlNode) {
    this.configuration = configuration;
    this.rootSqlNode = rootSqlNode;
  }

  /**
   * 获取一个BoundSql对象
   *
   * @author yangwenxin
   * @date 2023-06-06 10:47
   */
  @Override
  public BoundSql getBoundSql(Object parameterObject) {
    // 创建DynamicSqlSource的辅助类，用来记录DynamicSqlSource解析出来SQL片段信息和参数信息
    DynamicContext context = new DynamicContext(configuration, parameterObject);
    // 这里会从根节点开始，对节点逐层调用apply方法，经过这一步后，动态节点和"${}"都被替换
    rootSqlNode.apply(context);
    // 处理占位符、汇总参数信息
    SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
    Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
    // 使用SqlSourceBuilder处理"#{}"，将其转化为"?"，最终生成StaticSqlSource对象
    SqlSource sqlSource = sqlSourceParser.parse(context.getSql(), parameterType, context.getBindings());
    BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
    // 把context.getBindings()的参数信息放到boundSql的metaParameters中进行保存
    context.getBindings().forEach(boundSql::setAdditionalParameter);
    return boundSql;
  }

}
