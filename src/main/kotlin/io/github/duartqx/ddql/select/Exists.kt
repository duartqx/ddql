package io.github.duartqx.ddql.select

import io.github.duartqx.ddql.interfaces.Statement

/**
 * Represents a SQL EXISTS or NOT EXISTS expression.
 *
 * Wraps a subquery and evaluates whether it returns any rows.
 *
 * @property sql The subquery to evaluate.
 * @param negate Whether to negate the existence check. Defaults to `false`.
 */
public class Exists(public val sql: Sql, private var negate: Boolean = false) : Statement {

  /**
   * Toggles the existence check between EXISTS and NOT EXISTS.
   *
   * @return This expression with its existence check inverted.
   */
  public operator fun not(): Exists = apply { negate = !negate }

  /**
   * Builds and returns the EXISTS or NOT EXISTS expression.
   *
   * @return The generated SQL expression.
   */
  override fun toString(): String {
    val stmt = if (negate) "not exists" else "exists"
    return "$stmt ($sql)"
  }

  /**
   * Returns the existence expression as a SQL statement.
   *
   * @return The generated SQL expression.
   */
  override fun toStatementString(): String = toString()
}

/**
 * Creates an EXISTS expression from a subquery builder.
 *
 * Example:
 * ```kotlin
 * // EXISTS
 * exists {
 *   select("id") from "users"
 *   where("active" eq true)
 * }
 *
 * // NOT EXISTS
 * !exists {
 *   select("id") from "users"
 *   where("active" eq true)
 * }
 * ```
 *
 * @param block The builder used to construct the subquery.
 * @return An [Exists] expression wrapping the generated subquery.
 */
public fun exists(block: Sql.() -> Unit): Exists = Exists(Sql().apply(block))
