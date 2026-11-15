package io.github.duartqx.ddql.select

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
public class Sql : Statement {
  private var selection = "*"
  private var table = ""
  private var whereClause = ""
  private var groupByClause = ""
  private var havingClause = ""

  private val ctes = mutableListOf<Pair<String, Statement>>()
  private val joins = mutableListOf<JoinStatement>()
  private val orderBys = mutableListOf<OrderStatement>()

  /**
   * Adds a common table expression (CTE) to the query.
   *
   * The provided builder configures a separate [Sql] instance whose result is associated with the
   * given alias.
   *
   * @param alias The CTE name.
   * @param stmt The builder used to construct the CTE query.
   * @return This query instance.
   */
  public fun with(alias: String, stmt: Sql.() -> Unit): Sql = apply {
    ctes.add(alias to Sql().apply(stmt))
  }

  /**
   * Defines the columns or expressions to retrieve.
   *
   * Accepts strings, [Statement] instances, and other supported expressions. Defaults to `*` when
   * no columns are provided.
   *
   * @param columns The columns or expressions to select.
   * @return This query instance.
   */
  public fun select(vararg columns: Any): Sql = apply {
    selection = columns.joinToString(", ", transform = { Expr(it).expr }).ifEmpty { "*" }
  }

  /**
   * Defines the source table or expression for the query.
   *
   * @param source The table name, subquery, or supported SQL expression.
   * @return This query instance.
   */
  public infix fun from(source: Any): Sql = apply {
    table = Expr(source).expr
  }

  /**
   * Adds a JOIN clause to the query.
   *
   * @param statement The join definition.
   * @return This query instance.
   */
  public infix fun join(statement: JoinStatement): Sql = apply {
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
  public infix fun where(expr: Expr): Sql = apply {
    whereClause = "where ${expr.expr}"
  }

  /**
   * Defines the GROUP BY clause.
   *
   * @param expr The column or expression used to group rows.
   * @return This query instance.
   */
  public infix fun groupBy(expr: Any): Sql = apply {
    groupByClause = "group by ${Expr(expr).expr}"
  }

  /**
   * Defines the HAVING clause used to filter grouped results.
   *
   * @param expr The filtering expression.
   * @return This query instance.
   */
  public infix fun having(expr: Any): Sql = apply {
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
  public infix fun orderBy(builder: OrderBy.() -> Unit): Sql = apply {
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
    if (ctes.isNotEmpty()) add(ctes.toStatementString())

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
 * Converts CTE definitions into a SQL WITH clause.
 *
 * Each entry consists of a CTE alias and its corresponding statement.
 *
 * @return The formatted WITH clause.
 */
@JvmName("cteMutableListToStatementString")
internal fun MutableList<Pair<String, Statement>>.toStatementString(): String =
    joinToString(", ", prefix = "with ") { (alias, sql) ->
      "$alias as ($sql)"
    }

/**
 * Converts ordering definitions into a SQL ORDER BY clause.
 *
 * @return The formatted ORDER BY clause.
 */
@JvmName("orderByMutableListToStatementString")
internal fun MutableList<OrderStatement>.toStatementString(): String =
    joinToString(", ", prefix = "order by ", transform = OrderStatement::toOrderString)
