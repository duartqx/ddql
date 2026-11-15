package io.github.duartqx.ddql.select

import io.github.duartqx.ddql.expr.Expr
import io.github.duartqx.ddql.interfaces.JoinStatement

/**
 * Represents a SQL JOIN clause.
 *
 * Serves as the base class for LEFT, RIGHT, and INNER joins. Each join requires an ON condition
 * before being converted into SQL.
 *
 * @property target The table, subquery, or expression to join.
 */
public sealed class Join(public val target: Any) : JoinStatement {
  public lateinit var on: Expr

  /**
   * Defines the ON condition for the join.
   *
   * @param expr The expression used to match rows between tables.
   * @return This join instance.
   */
  public infix fun on(expr: Expr): Join = apply { on = expr }
}

/**
 * Represents a SQL LEFT JOIN.
 *
 * Returns all rows from the left side and matching rows from the right side. Unmatched right-side
 * columns are filled with NULL.
 *
 * @param target The table, subquery, or expression to join.
 */
public class Left(target: Any) : Join(target) {

  /**
   * Builds the LEFT JOIN clause.
   *
   * @return The generated SQL JOIN expression.
   */
  override fun toJoinString(): String = "left join ${Expr(target)} on $on"
}

/**
 * Represents a SQL RIGHT JOIN.
 *
 * Returns all rows from the right side and matching rows from the left side. Unmatched left-side
 * columns are filled with NULL.
 *
 * @param target The table, subquery, or expression to join.
 */
public class Right(target: Any) : Join(target) {

  /**
   * Builds the RIGHT JOIN clause.
   *
   * @return The generated SQL JOIN expression.
   */
  override fun toJoinString(): String = "right join ${Expr(target)} on $on"
}

/**
 * Represents a SQL INNER JOIN.
 *
 * Returns only rows with matching values on both sides of the join.
 *
 * @param target The table, subquery, or expression to join.
 */
public class Inner(target: Any) : Join(target) {

  /**
   * Builds the INNER JOIN clause.
   *
   * @return The generated SQL JOIN expression.
   */
  override fun toJoinString(): String = "join ${Expr(target)} on $on"
}

/**
 * Creates a LEFT JOIN for the specified target.
 *
 * Example:
 * ```kotlin
 * left("orders") on ("users.id" eq "orders.user_id")
 * ```
 *
 * @param target The table, subquery, or expression to join.
 * @return A [Left] join instance.
 */
public fun left(target: Any): Join = Left(target)

/**
 * Creates a RIGHT JOIN for the specified target.
 *
 * Example:
 * ```kotlin
 * right("orders") on ("users.id" eq "orders.user_id")
 * ```
 *
 * @param target The table, subquery, or expression to join.
 * @return A [Right] join instance.
 */
public fun right(target: Any): Join = Right(target)

/**
 * Creates an INNER JOIN for the specified target.
 *
 * Example:
 * ```kotlin
 * inner("orders") on ("users.id" eq orders.user_id")
 * ```
 *
 * @param target The table, subquery, or expression to join.
 * @return An [Inner] join instance.
 */
public fun inner(target: Any): Join = Inner(target)
