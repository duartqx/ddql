package io.github.duartqx.ddql.sql.dml

import io.github.duartqx.ddql.expr.Expr
import io.github.duartqx.ddql.interfaces.Statement

public class Dml {
  public fun insert(): Insert = Insert()
}

public class Insert : Statement {
  private var table = ""
  private var columns = listOf<String>()
  private var from: Statement? = null
  private var onConflict: OnConflict? = null
  private val returns: MutableList<Expr> = mutableListOf()

  public fun into(dest: String, vararg cols: String): Insert = apply {
    table = Expr(dest).expr
    columns = cols.toList()
  }

  public fun values(vararg args: List<Any>): Insert = apply {
    from = Values(*args)
  }

  public fun from(block: () -> Statement): Insert = apply {
    from = block()
  }

  public fun onConflict(block: OnConflict.() -> Unit): Insert = apply {
    onConflict = OnConflict().apply(block)
  }

  public fun returning(vararg columns: Any): Insert = apply {
    returns.addAll(columns.map { Expr(it) })
  }

  override fun toStatementString(): String {
    check(table.isNotEmpty()) { "You need to provide into what the insert will go" }
    check(columns.isNotEmpty()) { "You need to provide at least one column" }
    checkNotNull(from) { "You need to provide values or a select" }

    return buildList {
          add("insert into $table")
          add(columns.joinToString(", ", prefix = "(", postfix = ")"))
          add(from!!.toStatementString())
          if (onConflict != null) add(onConflict)
          if (returns.isNotEmpty()) add(returns.joinToString(", ", prefix = "returning "))
        }
        .joinToString(" ")
  }
}

public class Values(vararg values: List<Any>) : Statement {

  internal val values = values.toList()

  override fun toStatementString(): String {
    require(values.isNotEmpty()) { "Values must not be empty" }
    val valuesString =
        values.joinToString(separator = ", ") {
          it.joinToString(separator = ", ", prefix = "(", postfix = ")")
        }
    return "values $valuesString"
  }
}

public class OnConflict(vararg columns: String) {
  internal val columns = columns.toList()
  internal var action: Do? = null

  public fun doNothing() {
    action = Do.Nothing()
  }

  public fun doUpdate(block: Do.Update.() -> Unit) {
    action = Do.Update().apply(block)
  }

  override fun toString(): String {
    checkNotNull(action) { "You need to provide values or a update" }

    return buildList {
          add("on conflict")
          if (columns.isNotEmpty()) add(columns.joinToString(", ", prefix = "(", postfix = ")"))
          add(action)
        }
        .joinToString(" ")
  }
}

public sealed class Do {
  public class Nothing : Do() {
    override fun toString(): String = "do nothing"
  }

  public class Update : Do() {
    private val sets = mutableListOf<Pair<String, Expr>>()

    public fun set(column: String, value: Any) {
      sets.add(column to Expr(value))
    }

    public fun excluded(column: Any): String = "excluded.$column"

    override fun toString(): String {
      check(sets.isNotEmpty()) { "You need to provide what must be set" }
      val sets = sets.joinToString(", ") { "${it.first} = ${it.second}" }
      return "do update set $sets"
    }
  }
}
