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

import org.apache.ibatis.parsing.GenericTokenParser;
import org.apache.ibatis.parsing.TokenHandler;
import org.apache.ibatis.scripting.ScriptingException;
import org.apache.ibatis.type.SimpleTypeRegistry;

import java.util.regex.Pattern;

/**
 * TextSqlNode类对应了字符串节点，能够替换"${}"占位符
 *
 * @author Clinton Begin
 */
public class TextSqlNode implements SqlNode {
  private final String text;
  private final Pattern injectionFilter;

  public TextSqlNode(String text) {
    this(text, null);
  }

  public TextSqlNode(String text, Pattern injectionFilter) {
    this.text = text;
    this.injectionFilter = injectionFilter;
  }

  /**
   * 判断当前的TextSqlNode是不是动态的
   * 对于TextSqlNode对象而言，如果内部含有"${}"占位符，那它就是动态的
   *
   * @author yangwenxin
   * @date 2023-06-06 10:28
   */
  public boolean isDynamic() {
    // 占位符处理器，该处理器并不会处理占位符，而是判断是不是含有占位符
    DynamicCheckerTokenParser checker = new DynamicCheckerTokenParser();
    // 使用占位符处理器，如果节点内容中含有占位符，则DynamicCheckerTokenParser对象的isDynamic属性将会被设置为true
    GenericTokenParser parser = createParser(checker);
    parser.parse(text);
    return checker.isDynamic();
  }

  @Override
  public boolean apply(DynamicContext context) {
    // 创建通用的占位符解析器
    GenericTokenParser parser = createParser(new BindingTokenParser(context, injectionFilter));
    // 替换掉其中的${}占位符
    context.appendSql(parser.parse(text));
    return true;
  }

  /**
   * 创建一个通用的占位符解析器，用来解析${}占位符
   *
   * @author yangwenxin
   * @date 2023-06-06 10:25
   */
  private GenericTokenParser createParser(TokenHandler handler) {
    return new GenericTokenParser("${", "}", handler);
  }

  /**
   * 从上下文中取出"${}"占位符的变量名对应的变量值
   *
   * @author yangwenxin
   * @date 2023-06-06 10:27
   */
  private static class BindingTokenParser implements TokenHandler {

    private DynamicContext context;
    private Pattern injectionFilter;

    public BindingTokenParser(DynamicContext context, Pattern injectionFilter) {
      this.context = context;
      this.injectionFilter = injectionFilter;
    }

    /**
     * 该方法会取出占位符的变量，然后使用该变量作为键去上下文环境中寻找对应的值，之后会用找到的值替换占位符
     *
     * @author yangwenxin
     * @date 2023-06-06 10:22
     */
    @Override
    public String handleToken(String content) {
      Object parameter = context.getBindings().get("_parameter");
      if (parameter == null) {
        context.getBindings().put("value", null);
      } else if (SimpleTypeRegistry.isSimpleType(parameter.getClass())) {
        context.getBindings().put("value", parameter);
      }
      Object value = OgnlCache.getValue(content, context.getBindings());
      String srtValue = value == null ? "" : String.valueOf(value); // issue #274 return "" instead of "null"
      checkInjection(srtValue);
      return srtValue;
    }

    private void checkInjection(String value) {
      if (injectionFilter != null && !injectionFilter.matcher(value).matches()) {
        throw new ScriptingException("Invalid input. Please conform to regex" + injectionFilter.pattern());
      }
    }
  }

  private static class DynamicCheckerTokenParser implements TokenHandler {

    private boolean isDynamic;

    public DynamicCheckerTokenParser() {
      // Prevent Synthetic Access
    }

    public boolean isDynamic() {
      return isDynamic;
    }

    /**
     * 该方法会置位成员属性isDynamic，因此可以记录该对象是否遇到过占位符
     *
     * @author yangwenxin
     * @date 2023-06-06 10:23
     */
    @Override
    public String handleToken(String content) {
      this.isDynamic = true;
      return null;
    }
  }

}
