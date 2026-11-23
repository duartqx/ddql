package io.github.duartqx.ddql.sql

import io.github.duartqx.ddql.expr.Expr
import io.github.duartqx.ddql.interfaces.JoinStatement
import io.github.duartqx.ddql.interfaces.OrderStatement
import io.github.duartqx.ddql.interfaces.Statement

/**
 * Builds SQL SELECT statements using a fluent Kotlin DSL.
 *
 * Supports common query clauses, including CTEs, joins, filtering, grouping, aggregation, and
 * ordering.
 */
public open class Select(vararg columns: Any) : Statement {
  private var selection = columns.joinToString(", ", transform = { Expr(it).expr }).ifEmpty { "*" }
  private var table = ""
  private var whereClause = ""
  private var groupByClause = ""
  private var havingClause = ""

  private val joins = mutableListOf<JoinStatement>()
  private val orderBys = mutableListOf<OrderStatement>()

  /**
   * Defines the source table or expression for the query.
   *
   * @param source The table name, subquery, or supported SQL expression.
   * @return This query instance.
   */
  public fun from(source: Any): Select = apply {
    table = Expr(source).expr
  }

  /**
   * Adds a JOIN clause to the query.
   *
   * @param statement The join definition.
   * @return This query instance.
   */
  public fun join(statement: JoinStatement): Select = apply {
    joins.add(statement)
  }

  /**
   * Defines the WHERE clause used to filter rows.
   *
   * Replaces any previously defined WHERE clause.
   *
   * @param expr The filtering expression.
   * @return This query instance.
   */
  public fun where(expr: Expr): Select = apply {
    whereClause = "where ${expr.expr}"
  }

  /**
   * Defines the GROUP BY clause.
   *
   * @param expr The column or expression used to group rows.
   * @return This query instance.
   */
  public fun groupBy(expr: Any): Select = apply {
    groupByClause = "group by ${Expr(expr).expr}"
  }

  /**
   * Defines the HAVING clause used to filter grouped results.
   *
   * @param expr The filtering expression.
   * @return This query instance.
   */
  public fun having(expr: Any): Select = apply {
    havingClause = "having ${Expr(expr).expr}"
  }

  /**
   * Adds an ORDER BY clause to the query.
   *
   * Each invocation creates a separate ordering definition.
   *
   * @param builder The builder used to configure the ordering.
   * @return This query instance.
   */
  public fun orderBy(builder: OrderBy.() -> Unit): Select = apply {
    orderBys.add(OrderBy().apply(builder))
  }

  /**
   * Builds and returns the complete SQL query.
   *
   * Clauses are assembled in SQL order, omitting those that have not been defined.
   *
   * @return The generated SQL query.
   */
  public override fun toString(): String = buildList {
    add("select $selection")
    if (table.isNotEmpty()) add("from $table")
    addAll(joins.map(JoinStatement::toJoinString))
    add(whereClause)
    add(groupByClause)
    add(havingClause)

    if (orderBys.isNotEmpty()) add(orderBys.toStatementString())
  }
      .filter(String::isNotEmpty)
      .joinToString(" ")

  /**
   * Returns the generated SQL query as a statement.
   *
   * @return The complete SQL query.
   */
  public override fun toStatementString(): String = toString()
}

/**
 * Converts ordering definitions into a SQL ORDER BY clause.
 *
 * @return The formatted ORDER BY clause.
 */
@JvmName("orderByMutableListToStatementString")
internal fun MutableList<OrderStatement>.toStatementString(): String =
    joinToString(", ", prefix = "order by ", transform = OrderStatement::toOrderString)
