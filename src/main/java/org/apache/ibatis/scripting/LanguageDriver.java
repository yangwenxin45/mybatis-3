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
package org.apache.ibatis.scripting;

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.Configuration;

/**
 * 语言驱动类的接口，主要工作是生成SqlSource
 *
 * @author yangwenxin
 * @date 2023-06-05 15:01
 */
public interface LanguageDriver {

  /**
   * Creates a {@link ParameterHandler} that passes the actual parameters to the the JDBC statement.
   * 创建参数处理器，参数处理器能将实参传递给JDBC statement
   *
   * @param mappedStatement The mapped statement that is being executed 完整的数据库操作节点
   * @param parameterObject The input parameter object (can be null)  参数对象
   * @param boundSql        The resulting SQL once the dynamic language has been executed. 数据库操作语句转化的BoundSql对象
   * @return
   * @author Frank D. Martinez [mnesarco]
   * @see DefaultParameterHandler
   */
  ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql);

  /**
   * Creates an {@link SqlSource} that will hold the statement read from a mapper xml file.
   * It is called during startup, when the mapped statement is read from a class or an xml file.
   * 创建SqlSource对象（基于映射文件的方式）
   * 该方法在Mybatis启动阶段读取映射接口或映射文件时被调用
   *
   * @param configuration The MyBatis configuration 配置信息
   * @param script XNode parsed from a XML file 映射文件中的数据库操作节点
   * @param parameterType input parameter type got from a mapper method or specified in the parameterType xml attribute. Can be null. 参数类型
   * @return
   */
  SqlSource createSqlSource(Configuration configuration, XNode script, Class<?> parameterType);

  /**
   * Creates an {@link SqlSource} that will hold the statement read from an annotation.
   * It is called during startup, when the mapped statement is read from a class or an xml file.
   * 创建SqlSource对象（基于注解的方式）
   * 该方法在Mybatis启动阶段读取映射接口或映射文件时被调用
   *
   * @param configuration The MyBatis configuration
   * @param script The content of the annotation  注解中的SQL字符串
   * @param parameterType input parameter type got from a mapper method or specified in the parameterType xml attribute. Can be null.
   * @return
   */
  SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType);

}
