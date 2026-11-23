package io.github.duartqx.ddql.sql

import io.github.duartqx.ddql.interfaces.Statement

public typealias CteStatement = Pair<String, Statement>

public typealias CteStatements = List<CteStatement>

public open class Cte(
    private val cte: CteStatements,
    private val endStatement: Statement,
    private val prefix: String = "with ",
) : Statement {
  init {
    require(cte.isNotEmpty()) { "no statements specified" }
  }

  /**
   * Converts CTE definitions into a SQL WITH clause.
   *
   * Each entry consists of a CTE alias and its corresponding statement.
   *
   * @return The formatted WITH clause.
   */
  override fun toStatementString(): String = buildList {
    add(
        cte.joinToString(", ", prefix = prefix) { (alias, stmt) ->
          "$alias as (${stmt.toStatementString()})"
        }
    )
    add(endStatement.toStatementString())
  }
      .joinToString(" ")
}

public class RecursiveCte(cte: CteStatements, endStatement: Statement) :
    Cte(cte, endStatement, prefix = "with recursive ")

public fun with(vararg cte: CteStatement, endStatement: () -> Statement): Cte =
    Cte(cte.toList(), endStatement())

public fun withRecursive(
    vararg cte: CteStatement,
    endStatement: () -> Statement,
): RecursiveCte = RecursiveCte(cte.toList(), endStatement())
