package io.github.duartqx.ddql.sql

import io.github.duartqx.ddql.expr.Expr
import io.github.duartqx.ddql.interfaces.OrderStatement
import io.github.duartqx.ddql.interfaces.Statement

/**
 * Represents an ORDER BY expression with a target column or SQL expression.
 *
 * Serves as the base class for ascending ([Asc]) and descending ([Desc]) ordering definitions.
 *
 * @property target The column or expression used for ordering.
 */
public sealed class Order(public val target: Any) : OrderStatement, Statement

/**
 * Represents ascending SQL ordering using `ASC`.
 *
 * @param target The column or expression to sort by.
 */
public class Asc(target: Any) : Order(Expr(target)) {

  /** Returns the SQL expression with ascending ordering. */
  override fun toString(): String = Expr("$target asc").expr

  /** Returns the ordering expression. */
  override fun toOrderString(): String = toString()

  /** Returns the ordering expression as a SQL statement. */
  override fun toStatementString(): String = toString()
}

/**
 * Represents descending SQL ordering using `DESC`.
 *
 * @param target The column or expression to sort by.
 */
public class Desc(target: Any) : Order(Expr(target)) {

  /** Returns the SQL expression with descending ordering. */
  override fun toString(): String = Expr("$target desc").expr

  /** Returns the ordering expression. */
  override fun toOrderString(): String = toString()

  /** Returns the ordering expression as a SQL statement. */
  override fun toStatementString(): String = toString()
}

/**
 * Builds an SQL ORDER BY clause with multiple ordering expressions.
 *
 * Supports combining ascending and descending ordering definitions.
 *
 * Example:
 * ```kotlin
 * OrderBy().apply {
 *   asc("created_at")
 *   desc("name")
 * }
 * ```
 */
public class OrderBy : OrderStatement, Statement {
  private val orders = mutableListOf<OrderStatement>()

  /**
   * Adds an ascending ordering expression.
   *
   * @param target The column or expression to sort by.
   * @return This [OrderBy] instance.
   */
  public infix fun asc(target: Any): OrderBy = apply { orders.add(Asc(target)) }

  /**
   * Adds a descending ordering expression.
   *
   * @param target The column or expression to sort by.
   * @return This [OrderBy] instance.
   */
  public infix fun desc(target: Any): OrderBy = apply { orders.add(Desc(target)) }

  /**
   * Returns all ordering expressions separated by commas.
   *
   * @return The generated ORDER BY expressions.
   */
  override fun toString(): String = orders.joinToString(", ") { it.toOrderString() }

  /** Returns the ordering expressions. */
  override fun toOrderString(): String = toString()

  /** Returns the ordering expressions as a SQL statement. */
  override fun toStatementString(): String = toString()
}
