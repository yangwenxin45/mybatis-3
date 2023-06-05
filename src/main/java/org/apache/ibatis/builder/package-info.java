/**
 *    Copyright 2009-2015 the original author or authors.
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
/**
 * Base package for the Configuration building code
 * builder包是一个按照类型划分出来的包，具有以下两个功能：
 * 一是解析XML配置文件和映射文件，这部分功能在xml子包中
 * 而是解析注解形式的Mapper声明，这部分功能在annotation子包中
 */
package org.apache.ibatis.builder;
