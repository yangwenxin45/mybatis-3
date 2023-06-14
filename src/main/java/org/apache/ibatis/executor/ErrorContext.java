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
package org.apache.ibatis.executor;

/**
 * 错误上下文，能够提前将一些背景信息保存下来
 *
 * @author Clinton Begin
 */
public class ErrorContext {

  // 获取当前操作系统的换行符
  private static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");
  // 将自身存储进ThreadLocal，从而进行线程间的隔离
  private static final ThreadLocal<ErrorContext> LOCAL = new ThreadLocal<>();

  // 存储上一版本的自身，从而组成错误链
  private ErrorContext stored;

  // 下面几条为错误的详细信息，可以写入一项或者多项
  private String resource;
  private String activity;
  private String object;
  private String message;
  private String sql;
  private Throwable cause;

  private ErrorContext() {
  }

  /**
   * 从ThreadLocal取出已经实例化的ErrorContext，或者实例化一个ErrorContext放入ThreadLocal
   * 当需要获得当前线程的ErrorContext对象时调用
   *
   * @author yangwenxin
   * @date 2023-06-13 16:44
   */
  public static ErrorContext instance() {
    ErrorContext context = LOCAL.get();
    if (context == null) {
      context = new ErrorContext();
      LOCAL.set(context);
    }
    return context;
  }

  /**
   * 创建一个包装了原有ErrorContext的新ErrorContext
   * 当线程进入下一级操作并处于一个全新的环境时调用
   *
   * @author yangwenxin
   * @date 2023-06-13 16:45
   */
  public ErrorContext store() {
    ErrorContext newContext = new ErrorContext();
    newContext.stored = this;
    LOCAL.set(newContext);
    return LOCAL.get();
  }

  /**
   * 剥离当前ErrorContext的内部ErrorContext
   * 当线程从下一级操作返回上一级时调用
   *
   * @author yangwenxin
   * @date 2023-06-13 16:46
   */
  public ErrorContext recall() {
    if (stored != null) {
      LOCAL.set(stored);
      stored = null;
    }
    return LOCAL.get();
  }

  public ErrorContext resource(String resource) {
    this.resource = resource;
    return this;
  }

  public ErrorContext activity(String activity) {
    this.activity = activity;
    return this;
  }

  public ErrorContext object(String object) {
    this.object = object;
    return this;
  }

  public ErrorContext message(String message) {
    this.message = message;
    return this;
  }

  public ErrorContext sql(String sql) {
    this.sql = sql;
    return this;
  }

  public ErrorContext cause(Throwable cause) {
    this.cause = cause;
    return this;
  }

  /**
   * 当线程进入一个与之前操作无关的新环境时调用
   *
   * @author yangwenxin
   * @date 2023-06-13 16:48
   */
  public ErrorContext reset() {
    resource = null;
    activity = null;
    object = null;
    message = null;
    sql = null;
    cause = null;
    LOCAL.remove();
    return this;
  }

  /**
   * 当线程需要打印异常信息时调用
   *
   * @author yangwenxin
   * @date 2023-06-13 16:47
   */
  @Override
  public String toString() {
    StringBuilder description = new StringBuilder();

    // message
    if (this.message != null) {
      description.append(LINE_SEPARATOR);
      description.append("### ");
      description.append(this.message);
    }

    // resource
    if (resource != null) {
      description.append(LINE_SEPARATOR);
      description.append("### The error may exist in ");
      description.append(resource);
    }

    // object
    if (object != null) {
      description.append(LINE_SEPARATOR);
      description.append("### The error may involve ");
      description.append(object);
    }

    // activity
    if (activity != null) {
      description.append(LINE_SEPARATOR);
      description.append("### The error occurred while ");
      description.append(activity);
    }

    // activity
    if (sql != null) {
      description.append(LINE_SEPARATOR);
      description.append("### SQL: ");
      description.append(sql.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ').trim());
    }

    // cause
    if (cause != null) {
      description.append(LINE_SEPARATOR);
      description.append("### Cause: ");
      description.append(cause.toString());
    }

    return description.toString();
  }

}
