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
package org.apache.ibatis.jdbc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Clinton Begin
 * @author Jeff Butler
 * @author Adam Gent
 * @author Kazuki Shimizu
 */
public abstract class AbstractSQL<T> {

  private static final String AND = ") \nAND (";
  private static final String OR = ") \nOR (";

  private final SQLStatement sql = new SQLStatement();

  /**
   * 将AbstractSQL作为抽象方法独立出来，使得我们可以继承AbstractSQL实现其他的子类，保证了AbstractSQL类更容易扩展
   *
   * @author yangwenxin
   * @date 2023-06-07 09:42
   */
  public abstract T getSelf();

  public T UPDATE(String table) {
    sql().statementType = SQLStatement.StatementType.UPDATE;
    sql().tables.add(table);
    return getSelf();
  }

  public T SET(String sets) {
    sql().sets.add(sets);
    return getSelf();
  }

  /**
   * @since 3.4.2
   */
  public T SET(String... sets) {
    sql().sets.addAll(Arrays.asList(sets));
    return getSelf();
  }

  public T INSERT_INTO(String tableName) {
    sql().statementType = SQLStatement.StatementType.INSERT;
    sql().tables.add(tableName);
    return getSelf();
  }

  public T VALUES(String columns, String values) {
    INTO_COLUMNS(columns);
    INTO_VALUES(values);
    return getSelf();
  }

  /**
   * @since 3.4.2
   */
  public T INTO_COLUMNS(String... columns) {
    sql().columns.addAll(Arrays.asList(columns));
    return getSelf();
  }

  /**
   * @since 3.4.2
   */
  public T INTO_VALUES(String... values) {
    List<String> list = sql().valuesList.get(sql().valuesList.size() - 1);
    for (String value : values) {
      list.add(value);
    }
    return getSelf();
  }

  public T SELECT(String columns) {
    sql().statementType = SQLStatement.StatementType.SELECT;
    sql().select.add(columns);
    return getSelf();
  }

  /**
   * @since 3.4.2
   */
  public T SELECT(String... columns) {
    sql().statementType = SQLStatement.StatementType.SELECT;
    sql().select.addAll(Arrays.asList(columns));
    return getSelf();
  }

  public T SELECT_DISTINCT(String columns) {
    sql().distinct = true;
    SELECT(columns);
    return getSelf();
  }

  /**
   * @since 3.4.2
   */
  public T SELECT_DISTINCT(String... columns) {
    sql().distinct = true;
    SELECT(columns);
    return getSelf();
  }

  public T DELETE_FROM(String table) {
    sql().statementType = SQLStatement.StatementType.DELETE;
    sql().tables.add(table);
    return getSelf();
  }

  public T FROM(String table) {
    sql().tables.add(table);
    return getSelf();
  }

  /**
   * @since 3.4.2
   */
  public T FROM(String... tables) {
    sql().tables.addAll(Arrays.asList(tables));
    return getSelf();
  }

  public T JOIN(String join) {
    sql().join.add(join);
    return getSelf();
  }

  /**
   * @since 3.4.2
   */
  public T JOIN(String... joins) {
    sql().join.addAll(Arrays.asList(joins));
    return getSelf();
  }

  public T INNER_JOIN(String join) {
    sql().innerJoin.add(join);
    return getSelf();
  }

  /**
   * @since 3.4.2
   */
  public T INNER_JOIN(String... joins) {
    sql().innerJoin.addAll(Arrays.asList(joins));
    return getSelf();
  }

  public T LEFT_OUTER_JOIN(String join) {
    sql().leftOuterJoin.add(join);
    return getSelf();
  }

  /**
   * @since 3.4.2
   */
  public T LEFT_OUTER_JOIN(String... joins) {
    sql().leftOuterJoin.addAll(Arrays.asList(joins));
    return getSelf();
  }

  public T RIGHT_OUTER_JOIN(String join) {
    sql().rightOuterJoin.add(join);
    return getSelf();
  }

  /**
   * @since 3.4.2
   */
  public T RIGHT_OUTER_JOIN(String... joins) {
    sql().rightOuterJoin.addAll(Arrays.asList(joins));
    return getSelf();
  }

  public T OUTER_JOIN(String join) {
    sql().outerJoin.add(join);
    return getSelf();
  }

  /**
   * @since 3.4.2
   */
  public T OUTER_JOIN(String... joins) {
    sql().outerJoin.addAll(Arrays.asList(joins));
    return getSelf();
  }

  public T WHERE(String conditions) {
    sql().where.add(conditions);
    sql().lastList = sql().where;
    return getSelf();
  }

  /**
   * @since 3.4.2
   */
  public T WHERE(String... conditions) {
    sql().where.addAll(Arrays.asList(conditions));
    sql().lastList = sql().where;
    return getSelf();
  }

  public T OR() {
    sql().lastList.add(OR);
    return getSelf();
  }

  public T AND() {
    sql().lastList.add(AND);
    return getSelf();
  }

  public T GROUP_BY(String columns) {
    sql().groupBy.add(columns);
    return getSelf();
  }

  /**
   * @since 3.4.2
   */
  public T GROUP_BY(String... columns) {
    sql().groupBy.addAll(Arrays.asList(columns));
    return getSelf();
  }

  public T HAVING(String conditions) {
    sql().having.add(conditions);
    sql().lastList = sql().having;
    return getSelf();
  }

  /**
   * @since 3.4.2
   */
  public T HAVING(String... conditions) {
    sql().having.addAll(Arrays.asList(conditions));
    sql().lastList = sql().having;
    return getSelf();
  }

  public T ORDER_BY(String columns) {
    sql().orderBy.add(columns);
    return getSelf();
  }

  /**
   * @since 3.4.2
   */
  public T ORDER_BY(String... columns) {
    sql().orderBy.addAll(Arrays.asList(columns));
    return getSelf();
  }

  /**
   * Set the limit variable string(e.g. {@code "#{limit}"}).
   *
   * @param variable a limit variable string
   * @return a self instance
   * @see #OFFSET(String)
   * @since 3.5.2
   */
  public T LIMIT(String variable) {
    sql().limit = variable;
    sql().limitingRowsStrategy = SQLStatement.LimitingRowsStrategy.OFFSET_LIMIT;
    return getSelf();
  }

  /**
   * Set the limit value.
   *
   * @param value an offset value
   * @return a self instance
   * @see #OFFSET(long)
   * @since 3.5.2
   */
  public T LIMIT(int value) {
    return LIMIT(String.valueOf(value));
  }

  /**
   * Set the offset variable string(e.g. {@code "#{offset}"}).
   *
   * @param variable a offset variable string
   * @return a self instance
   * @see #LIMIT(String)
   * @since 3.5.2
   */
  public T OFFSET(String variable) {
    sql().offset = variable;
    sql().limitingRowsStrategy = SQLStatement.LimitingRowsStrategy.OFFSET_LIMIT;
    return getSelf();
  }

  /**
   * Set the offset value.
   *
   * @param value an offset value
   * @return a self instance
   * @see #LIMIT(int)
   * @since 3.5.2
   */
  public T OFFSET(long value) {
    return OFFSET(String.valueOf(value));
  }

  /**
   * Set the fetch first rows variable string(e.g. {@code "#{fetchFirstRows}"}).
   *
   * @param variable a fetch first rows variable string
   * @return a self instance
   * @see #OFFSET_ROWS(String)
   * @since 3.5.2
   */
  public T FETCH_FIRST_ROWS_ONLY(String variable) {
    sql().limit = variable;
    sql().limitingRowsStrategy = SQLStatement.LimitingRowsStrategy.ISO;
    return getSelf();
  }

  /**
   * Set the fetch first rows value.
   *
   * @param value a fetch first rows value
   * @return a self instance
   * @see #OFFSET_ROWS(long)
   * @since 3.5.2
   */
  public T FETCH_FIRST_ROWS_ONLY(int value) {
    return FETCH_FIRST_ROWS_ONLY(String.valueOf(value));
  }

  /**
   * Set the offset rows variable string(e.g. {@code "#{offset}"}).
   *
   * @param variable a offset rows variable string
   * @return a self instance
   * @see #FETCH_FIRST_ROWS_ONLY(String)
   * @since 3.5.2
   */
  public T OFFSET_ROWS(String variable) {
    sql().offset = variable;
    sql().limitingRowsStrategy = SQLStatement.LimitingRowsStrategy.ISO;
    return getSelf();
  }

  /**
   * Set the offset rows value.
   *
   * @param value an offset rows value
   * @return a self instance
   * @see #FETCH_FIRST_ROWS_ONLY(int)
   * @since 3.5.2
   */
  public T OFFSET_ROWS(long value) {
    return OFFSET_ROWS(String.valueOf(value));
  }

  /*
   * used to add a new inserted row while do multi-row insert.
   *
   * @since 3.5.2
   */
  public T ADD_ROW() {
    sql().valuesList.add(new ArrayList<>());
    return getSelf();
  }

  private SQLStatement sql() {
    return sql;
  }

  public <A extends Appendable> A usingAppender(A a) {
    sql().sql(a);
    return a;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sql().sql(sb);
    return sb.toString();
  }

  /**
   * 拼接器，实现串的拼接功能
   *
   * @author yangwenxin
   * @date 2023-06-07 09:24
   */
  private static class SafeAppendable {
    // 主串
    private final Appendable a;
    // 主串是否为空
    private boolean empty = true;

    public SafeAppendable(Appendable a) {
      super();
      this.a = a;
    }

    public SafeAppendable append(CharSequence s) {
      try {
        // 要拼接的串长度不为零，则拼完后主串也不是空了
        if (empty && s.length() > 0) {
          empty = false;
        }
        a.append(s);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return this;
    }

    public boolean isEmpty() {
      return empty;
    }

  }

  /**
   * 完整地表述出一条SQL语句
   *
   * @author yangwenxin
   * @date 2023-06-07 09:27
   */
  private static class SQLStatement {

    public enum StatementType {
      DELETE, INSERT, SELECT, UPDATE
    }

    private enum LimitingRowsStrategy {
      NOP {
        @Override
        protected void appendClause(SafeAppendable builder, String offset, String limit) {
          // NOP
        }
      },
      ISO {
        @Override
        protected void appendClause(SafeAppendable builder, String offset, String limit) {
          if (offset != null) {
            builder.append(" OFFSET ").append(offset).append(" ROWS");
          }
          if (limit != null) {
            builder.append(" FETCH FIRST ").append(limit).append(" ROWS ONLY");
          }
        }
      },
      OFFSET_LIMIT {
        @Override
        protected void appendClause(SafeAppendable builder, String offset, String limit) {
          if (limit != null) {
            builder.append(" LIMIT ").append(limit);
          }
          if (offset != null) {
            builder.append(" OFFSET ").append(offset);
          }
        }
      };

      protected abstract void appendClause(SafeAppendable builder, String offset, String limit);

    }

    // 当前语句的语句类型
    StatementType statementType;
    // 语句片段信息
    List<String> sets = new ArrayList<>();
    List<String> select = new ArrayList<>();
    List<String> tables = new ArrayList<>();
    List<String> join = new ArrayList<>();
    List<String> innerJoin = new ArrayList<>();
    List<String> outerJoin = new ArrayList<>();
    List<String> leftOuterJoin = new ArrayList<>();
    List<String> rightOuterJoin = new ArrayList<>();
    List<String> where = new ArrayList<>();
    List<String> having = new ArrayList<>();
    List<String> groupBy = new ArrayList<>();
    List<String> orderBy = new ArrayList<>();
    List<String> lastList = new ArrayList<>();
    List<String> columns = new ArrayList<>();
    List<List<String>> valuesList = new ArrayList<>();
    // 表名是否去重，该字段仅对于select操作有效，决定是select还是select distinct
    boolean distinct;
    // 结果偏移量
    String offset;
    // 结果总数约束
    String limit;
    // 结果约束策略
    LimitingRowsStrategy limitingRowsStrategy = LimitingRowsStrategy.NOP;

    public SQLStatement() {
      // Prevent Synthetic Access
      valuesList.add(new ArrayList<>());
    }

    private void sqlClause(SafeAppendable builder, String keyword, List<String> parts, String open, String close,
                           String conjunction) {
      if (!parts.isEmpty()) {
        if (!builder.isEmpty()) {
          builder.append("\n");
        }
        builder.append(keyword);
        builder.append(" ");
        builder.append(open);
        String last = "________";
        for (int i = 0, n = parts.size(); i < n; i++) {
          String part = parts.get(i);
          if (i > 0 && !part.equals(AND) && !part.equals(OR) && !last.equals(AND) && !last.equals(OR)) {
            builder.append(conjunction);
          }
          builder.append(part);
          last = part;
        }
        builder.append(close);
      }
    }

    /**
     * 将SQL语句片段拼接为一个完整地SELECT语句
     *
     * @author yangwenxin
     * @date 2023-06-07 09:34
     */
    private String selectSQL(SafeAppendable builder) {
      if (distinct) {
        sqlClause(builder, "SELECT DISTINCT", select, "", "", ", ");
      } else {
        sqlClause(builder, "SELECT", select, "", "", ", ");
      }

      sqlClause(builder, "FROM", tables, "", "", ", ");
      // JOIN操作相对复杂，调用单独的joins子方法进行操作
      joins(builder);
      sqlClause(builder, "WHERE", where, "(", ")", " AND ");
      sqlClause(builder, "GROUP BY", groupBy, "", "", ", ");
      sqlClause(builder, "HAVING", having, "(", ")", " AND ");
      sqlClause(builder, "ORDER BY", orderBy, "", "", ", ");
      limitingRowsStrategy.appendClause(builder, offset, limit);
      return builder.toString();
    }

    private void joins(SafeAppendable builder) {
      sqlClause(builder, "JOIN", join, "", "", "\nJOIN ");
      sqlClause(builder, "INNER JOIN", innerJoin, "", "", "\nINNER JOIN ");
      sqlClause(builder, "OUTER JOIN", outerJoin, "", "", "\nOUTER JOIN ");
      sqlClause(builder, "LEFT OUTER JOIN", leftOuterJoin, "", "", "\nLEFT OUTER JOIN ");
      sqlClause(builder, "RIGHT OUTER JOIN", rightOuterJoin, "", "", "\nRIGHT OUTER JOIN ");
    }

    /**
     * 将SQL语句片段拼接为一个完整的INSERT语句
     *
     * @author yangwenxin
     * @date 2023-06-07 09:39
     */
    private String insertSQL(SafeAppendable builder) {
      sqlClause(builder, "INSERT INTO", tables, "", "", "");
      sqlClause(builder, "", columns, "(", ")", ", ");
      for (int i = 0; i < valuesList.size(); i++) {
        sqlClause(builder, i > 0 ? "," : "VALUES", valuesList.get(i), "(", ")", ", ");
      }
      return builder.toString();
    }

    private String deleteSQL(SafeAppendable builder) {
      sqlClause(builder, "DELETE FROM", tables, "", "", "");
      sqlClause(builder, "WHERE", where, "(", ")", " AND ");
      limitingRowsStrategy.appendClause(builder, null, limit);
      return builder.toString();
    }

    private String updateSQL(SafeAppendable builder) {
      sqlClause(builder, "UPDATE", tables, "", "", "");
      joins(builder);
      sqlClause(builder, "SET", sets, "", "", ", ");
      sqlClause(builder, "WHERE", where, "(", ")", " AND ");
      limitingRowsStrategy.appendClause(builder, null, limit);
      return builder.toString();
    }

    /**
     * 根据语句类型，调用不同的语句拼接器拼接SQL语句
     *
     * @param a 起始字符串
     * @return 拼接完成后的结果
     */
    public String sql(Appendable a) {
      SafeAppendable builder = new SafeAppendable(a);
      if (statementType == null) {
        return null;
      }

      String answer;

      switch (statementType) {
        case DELETE:
          answer = deleteSQL(builder);
          break;

        case INSERT:
          answer = insertSQL(builder);
          break;

        case SELECT:
          answer = selectSQL(builder);
          break;

        case UPDATE:
          answer = updateSQL(builder);
          break;

        default:
          answer = null;
      }

      return answer;
    }
  }
}
