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
/**
 * Bings mapper interfaces with mapped statements
 * binding包主要用来处理Java方法与SQL语句之间绑定关系的包
 * binding包具有以下两个功能：
 * 1. 维护映射接口中抽象方法与数据库操作节点之间的关联关系
 * 2. 为映射接口中的抽象方法接入对应的数据库操作
 */
package org.apache.ibatis.binding;
