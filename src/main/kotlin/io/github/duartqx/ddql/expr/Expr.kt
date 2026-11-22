package io.github.duartqx.ddql.expr

import io.github.duartqx.ddql.interfaces.Statement

/**
 * Represents a raw SQL expression.
 *
 * Accepts strings, statements, aliased expressions, and other values. SQL comments and raw string
 * literals are rejected to encourage the use of named parameters.
 *
 * @property expr The resulting SQL expression.
 * @param expr The expression source. Can be a [String], [Statement], [Pair] for aliases, or another
 *   object converted to a string.
 * @constructor Creates an expression from the provided value.
 * @throws IllegalArgumentException if the resulting SQL contains potentially unsafe syntax.
 */
public class Expr(expr: Any?) : Statement {
  public val expr: String = run {
    val rawSql =
        when (expr) {
          is String -> expr
          is Statement -> expr.toStatementString()
          is Pair<*, *> -> "${Expr(expr.first)} as ${Expr(expr.second)}"
          else -> expr.toString()
        }

    require(isSafe(rawSql)) { "Potential SQL injection: $rawSql" }

    rawSql
  }

  /** Returns the SQL expression. */
  override fun toStatementString(): String = expr

  /** Returns the SQL expression. */
  override fun toString(): String = expr

  /**
   * Combines this expression with another using SQL `AND`.
   *
   * Both expressions are wrapped in parentheses.
   *
   * @param other The expression to combine with.
   * @return A new combined expression.
   */
  public infix fun and(other: Any): Expr = Expr("($expr and $other)")

  /**
   * Combines this expression with another using SQL `OR`.
   *
   * Both expressions are wrapped in parentheses.
   *
   * @param other The expression to combine with.
   * @return A new combined expression.
   */
  public infix fun or(other: Any): Expr = Expr("($expr or $other)")
}

/** Provides SQL aliasing through the Kotlin infix `as` function. */
public object As {
  /**
   * Creates an alias for a column or expression.
   *
   * @param alias The alias name.
   * @return A pair containing the original expression and its alias.
   */
  public infix fun String.`as`(alias: String): Pair<String, String> = this to alias

  public infix fun String.`as`(stmt: Statement): Pair<String, Statement> = this to stmt
}

/** Provides SQL equality comparison operators. */
public object Eq {
  private fun eq(right: Any, left: Any): Expr = Expr("$right = $left")

  /**
   * Compares this column with a value or expression using `=`.
   *
   * @param param The value or expression to compare against.
   * @return An equality expression.
   */
  public infix fun String.eq(param: Any): Expr = eq(this, param)

  /**
   * Creates an equality comparison using a named parameter.
   *
   * The parameter name is prefixed with `:`.
   *
   * @param param The named parameter.
   * @return An equality expression.
   */
  public fun eq(param: Any): Expr = eq(param, ":$param")
}

/** Provides SQL inequality comparison operators. */
public object Neq {
  private fun neq(right: Any, left: Any): Expr = Expr("$right != $left")

  /** Compares this column with a value or expression using `!=`. */
  public infix fun String.neq(param: Any): Expr = neq(this, param)

  /** Creates an inequality comparison using a named parameter. */
  public fun neq(param: Any): Expr = neq(param, ":$param")
}

/** Provides SQL greater-than comparison operators. */
public object Gt {
  private fun gt(right: Any, left: Any): Expr = Expr("$right > $left")

  /** Checks whether this column is greater than the given value or expression. */
  public infix fun String.gt(param: Any): Expr = gt(this, param)

  /** Creates a greater-than comparison using a named parameter. */
  public fun gt(param: Any): Expr = gt(param, ":$param")
}

/** Provides SQL less-than comparison operators. */
public object Lt {
  private fun lt(right: Any, left: Any): Expr = Expr("$right < $left")

  /** Checks whether this column is less than the given value or expression. */
  public infix fun String.lt(param: Any): Expr = lt(this, param)

  /** Creates a less-than comparison using a named parameter. */
  public fun lt(param: Any): Expr = lt(param, ":$param")
}

/** Provides SQL greater-than-or-equal comparison operators. */
public object Gte {
  private fun gte(right: Any, left: Any): Expr = Expr("$right >= $left")

  /** Checks whether this column is greater than or equal to the given value or expression. */
  public infix fun String.gte(param: Any): Expr = gte(this, param)

  /** Creates a greater-than-or-equal comparison using a named parameter. */
  public fun gte(param: Any): Expr = gte(param, ":$param")
}

/** Provides SQL less-than-or-equal comparison operators. */
public object Lte {
  private fun lte(right: Any, left: Any): Expr = Expr("$right <= $left")

  /** Checks whether this column is less than or equal to the given value or expression. */
  public infix fun String.lte(param: Any): Expr = lte(this, param)

  /** Creates a less-than-or-equal comparison using a named parameter. */
  public fun lte(param: Any): Expr = lte(param, ":$param")
}

/** Provides SQL NULL comparison operators. */
public object Null {
  /** Checks whether this column contains a SQL `NULL` value. */
  public fun String.isNull(): Expr = Expr("$this is null")

  /** Checks whether this column does not contain a SQL `NULL` value. */
  public fun String.isNotNull(): Expr = Expr("$this is not null")
}

/**
 * Provides SQL DISTINCT FROM comparison operators.
 *
 * Unlike regular equality operators, these operators treat NULL as a comparable value, following
 * SQL's NULL-safe comparison semantics.
 */
public object Distinct {
  private fun isDistinctFrom(right: Any, left: Any): Expr = Expr("$left is distinct from $right")

  private fun isNotDistinctFrom(right: Any, left: Any): Expr =
      Expr("$left is not distinct from $right")

  /**
   * Checks whether this expression is distinct from another expression.
   *
   * NULL is treated as a comparable value.
   *
   * @param left The value or expression to compare against.
   * @return A `IS DISTINCT FROM` expression.
   */
  public infix fun String.isDistinctFrom(left: Any): Expr = isDistinctFrom(this, left)

  /**
   * Checks whether this expression is not distinct from another expression.
   *
   * NULL is treated as a comparable value.
   *
   * @param left The value or expression to compare against.
   * @return A `IS NOT DISTINCT FROM` expression.
   */
  public infix fun String.isNotDistinctFrom(left: Any): Expr = isNotDistinctFrom(this, left)

  /** Creates an `IS DISTINCT FROM` comparison using a named parameter. */
  public fun isDistinctFrom(right: Any): Expr = isDistinctFrom(right, ":$right")

  /** Creates an `IS NOT DISTINCT FROM` comparison using a named parameter. */
  public fun isNotDistinctFrom(right: Any): Expr = isNotDistinctFrom(right, ":$right")
}

/** Provides SQL IN comparison operators. */
public object In {
  private fun `in`(right: Any, left: Any): Expr = Expr("$right in ($left)")

  /**
   * Creates an `IN` expression using a named parameter.
   *
   * @param param The named parameter.
   * @return An `IN` expression.
   */
  public fun `in`(param: Any): Expr = `in`(param, ":$param")

  /**
   * Checks whether this expression matches any value in the given expression.
   *
   * @param value The value, parameter, or expression to match against.
   * @return An `IN` expression.
   */
  public infix fun String.`in`(value: Any): Expr = `in`(this, value)
}

/**
 * Checks whether a SQL expression contains only allowed syntax.
 *
 * Rejects semicolons, SQL comments, and raw single-quoted string literals, while allowing
 * single-quoted JSONB key accessors.
 *
 * @param sql The SQL expression to validate.
 * @return `true` if the SQL expression is considered safe; `false` otherwise.
 */
private fun isSafe(sql: String): Boolean {
  if (sql.contains(";")) return false

  // Block SQL comments (--, #, /* ... */, */)
  val commentPattern = Regex("(--|#|/\\*|\\*/)")
  if (commentPattern.containsMatchIn(sql)) return false

  // Allow valid JSONB key accessors (e.g., data->'key' or data->>'key').
  val jsonbCleaned = sql.replace(Regex("(->|->>|#>|#>>)\\s*'[^']*'"), "___JSONB_KEY___")

  // Reject raw string literals.
  return !jsonbCleaned.contains("'")
}
