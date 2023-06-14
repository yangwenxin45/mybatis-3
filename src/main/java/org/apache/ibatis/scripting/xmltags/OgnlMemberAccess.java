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

import ognl.MemberAccess;
import org.apache.ibatis.reflection.Reflector;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.util.Map;

/**
 * The {@link MemberAccess} class that based on <a href=
 * 'https://github.com/jkuhnert/ognl/blob/OGNL_3_2_1/src/java/ognl/DefaultMemberAccess.java'>DefaultMemberAccess</a>.
 * OGNL借助这个接口为访问对象的属性做好准备
 *
 * @author Kazuki Shimizu
 * @see <a href=
 * 'https://github.com/jkuhnert/ognl/blob/OGNL_3_2_1/src/java/ognl/DefaultMemberAccess.java'>DefaultMemberAccess</a>
 * @see <a href='https://github.com/jkuhnert/ognl/issues/47'>#47 of ognl</a>
 * @since 3.5.0
 */
class OgnlMemberAccess implements MemberAccess {

  // 当前环境下，通过反射是否能够修改对象属性的可访问性
  private final boolean canControlMemberAccessible;

  OgnlMemberAccess() {
    this.canControlMemberAccessible = Reflector.canControlMemberAccessible();
  }

  /**
   * 设置属性的可访问性
   *
   * @param context      环境上下文
   * @param target       目标对象
   * @param member       目标对象的目标成员
   * @param propertyName 属性名称
   * @return 属性的可访问性
   */
  @Override
  public Object setup(Map context, Object target, Member member, String propertyName) {
    Object result = null;
    // 如果允许修改属性的可访问性
    if (isAccessible(context, target, member, propertyName)) {
      AccessibleObject accessible = (AccessibleObject) member;
      // 如果属性原本不可访问
      if (!accessible.isAccessible()) {
        result = Boolean.FALSE;
        // 将属性修改为可访问
        accessible.setAccessible(true);
      }
    }
    return result;
  }

  /**
   * 将属性的可访问性恢复到指定状态
   *
   * @param context
   * @param target
   * @param member
   * @param propertyName
   * @param state        指定的状态
   */
  @Override
  public void restore(Map context, Object target, Member member, String propertyName,
                      Object state) {
    if (state != null) {
      ((AccessibleObject) member).setAccessible((Boolean) state);
    }
  }

  /**
   * 判断对象属性是否可访问
   *
   * @author yangwenxin
   * @date 2023-06-05 16:26
   */
  @Override
  public boolean isAccessible(Map context, Object target, Member member, String propertyName) {
    return canControlMemberAccessible;
  }

}
