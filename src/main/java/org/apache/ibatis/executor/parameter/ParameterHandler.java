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
package org.apache.ibatis.executor.parameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 提供参数处理功能
 * A parameter handler sets the parameters of the {@code PreparedStatement}.
 *
 * @author Clinton Begin
 */
public interface ParameterHandler {

  /**
   * 用来获取SQL语句对应的实参对象
   *
   * @author yangwenxin
   * @date 2023-06-13 14:46
   */
  Object getParameterObject();

  /**
   * 用来完成SQL语句中的变量赋值
   *
   * @author yangwenxin
   * @date 2023-06-13 14:46
   */
  void setParameters(PreparedStatement ps)
    throws SQLException;

}
