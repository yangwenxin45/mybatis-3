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
package org.apache.ibatis.datasource.jndi;

import org.apache.ibatis.datasource.DataSourceException;
import org.apache.ibatis.datasource.DataSourceFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * JndiDataSourceFactory的作用是从环境中找出指定的JNDI数据源
 *
 * @author Clinton Begin
 */
public class JndiDataSourceFactory implements DataSourceFactory {

  // 起始环境信息，Mybatis会到这里寻找指定的数据源，不设置的话Mybatis会在整个环境中寻找数据源
  public static final String INITIAL_CONTEXT = "initial_context";
  // 数据源JNDI名称
  public static final String DATA_SOURCE = "data_source";
  // 以".env"开头的其他环境配置信息
  public static final String ENV_PREFIX = "env.";

  private DataSource dataSource;

  /**
   * 配置数据源属性，其中包含了数据源的查找工作
   *
   * @author yangwenxin
   * @date 2023-06-06 14:10
   */
  @Override
  public void setProperties(Properties properties) {
    try {
      // 初始化上下文环境
      InitialContext initCtx;
      // 获取配置信息，根据配置信息初始化环境
      Properties env = getEnvProperties(properties);
      if (env == null) {
        initCtx = new InitialContext();
      } else {
        initCtx = new InitialContext(env);
      }

      // 从配置信息中获取数据源信息
      if (properties.containsKey(INITIAL_CONTEXT)
        && properties.containsKey(DATA_SOURCE)) {
        // 定位到initial_context给出的起始环境
        Context ctx = (Context) initCtx.lookup(properties.getProperty(INITIAL_CONTEXT));
        // 从起始环境中寻找指定数据源
        dataSource = (DataSource) ctx.lookup(properties.getProperty(DATA_SOURCE));
      } else if (properties.containsKey(DATA_SOURCE)) {
        // 从整个环境中寻找指定数据源
        dataSource = (DataSource) initCtx.lookup(properties.getProperty(DATA_SOURCE));
      }

    } catch (NamingException e) {
      throw new DataSourceException("There was an error configuring JndiDataSourceTransactionPool. Cause: " + e, e);
    }
  }

  @Override
  public DataSource getDataSource() {
    return dataSource;
  }

  private static Properties getEnvProperties(Properties allProps) {
    final String PREFIX = ENV_PREFIX;
    Properties contextProperties = null;
    for (Entry<Object, Object> entry : allProps.entrySet()) {
      String key = (String) entry.getKey();
      String value = (String) entry.getValue();
      if (key.startsWith(PREFIX)) {
        if (contextProperties == null) {
          contextProperties = new Properties();
        }
        contextProperties.put(key.substring(PREFIX.length()), value);
      }
    }
    return contextProperties;
  }

}
