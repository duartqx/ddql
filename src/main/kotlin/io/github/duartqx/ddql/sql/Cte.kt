package io.github.duartqx.ddql.sql

import io.github.duartqx.ddql.interfaces.Statement

public typealias CteStatement = Pair<String, Statement>

public typealias CteStatements = List<CteStatement>

public open class Cte(
    private val statements: CteStatements,
    private val prefix: String = "with ",
) : Statement {
  init {
    require(statements.isNotEmpty()) { "no statements specified" }
  }

  /**
   * Converts CTE definitions into a SQL WITH clause.
   *
   * Each entry consists of a CTE alias and its corresponding statement.
   *
   * @return The formatted WITH clause.
   */
  override fun toStatementString(): String =
      statements.joinToString(", ", prefix = prefix) { (alias, sql) ->
        "$alias as ($sql)"
      }
}

public class RecursiveCte(statements: CteStatements) : Cte(statements, prefix = "with recursive ")

public fun CteStatements.toCte(): Cte = Cte(this)

public fun recursive(vararg statements: Pair<String, Statement>): RecursiveCte =
    RecursiveCte(statements.toList())
