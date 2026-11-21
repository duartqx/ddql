package io.github.duartqx.ddql.sql

import io.github.duartqx.ddql.expr.Expr
import io.github.duartqx.ddql.interfaces.Statement

/**
 * Builds a SQL CASE expression.
 *
 * Supports multiple WHEN/THEN branches and an optional ELSE clause.
 *
 * A CASE expression must contain at least one WHEN branch before it can be converted into SQL.
 */
public class Case : Statement {
  private val whenStatements = mutableListOf<When>()
  private var elseStatement = ""

  /**
   * Adds a WHEN branch to the CASE expression.
   *
   * @param expr The condition to evaluate.
   * @return A [When] instance used to define the corresponding THEN branch.
   */
  public fun `when`(expr: Any): When = When(Expr(expr)).also { whenStatements.add(it) }

  /**
   * Defines the ELSE branch of the CASE expression.
   *
   * @param expr The expression returned when none of the WHEN conditions match.
   * @return This CASE expression.
   */
  public infix fun `else`(expr: Any): Case = apply { elseStatement = Expr(expr).expr }

  /**
   * Builds and returns the complete SQL CASE expression.
   *
   * @return The generated CASE expression.
   * @throws IllegalArgumentException if no WHEN branches have been defined.
   */
  override fun toString(): String {
    require(whenStatements.isNotEmpty()) { "Case statement requires options" }

    return buildList {
          add("case")
          add(whenStatements.joinToString(" ", transform = When::toStatementString))
          if (elseStatement.isNotEmpty()) add("else $elseStatement")
          add("end")
        }
        .joinToString(" ")
  }

  /**
   * Returns the CASE expression as a SQL statement.
   *
   * @return The generated CASE expression.
   */
  override fun toStatementString(): String = toString()
}

/**
 * Represents a WHEN/THEN branch within a [Case] expression.
 *
 * A THEN expression must be defined before converting this branch into SQL.
 *
 * @param expr The condition evaluated by the WHEN clause.
 */
public class When(private val expr: Expr) : Statement {
  private lateinit var then: Expr

  /**
   * Defines the expression returned when the WHEN condition matches.
   *
   * @param target The expression evaluated by the THEN clause.
   * @return This WHEN branch.
   */
  public infix fun then(target: Any): When = apply { then = Expr(target) }

  /**
   * Builds and returns the WHEN/THEN branch.
   *
   * @return The generated SQL branch.
   */
  override fun toString(): String = "when $expr then $then"

  /**
   * Returns the WHEN/THEN branch as a SQL statement.
   *
   * @return The generated SQL branch.
   */
  override fun toStatementString(): String = toString()
}

/**
 * Creates and configures a SQL CASE expression.
 *
 * Example:
 * ```kotlin
 * case {
 *   `when`("status = 'active'") then ":enabled"
 *   `when`("status = 'inactive'") then ":disabled"
 *   `else`("'Unknown'")
 * }
 * ```
 *
 * @param block The builder used to configure the CASE expression.
 * @return The configured [Case] expression.
 */
public fun case(block: Case.() -> Unit): Case = Case().apply(block)
