package io.github.duartqx.ddql

import io.github.duartqx.ddql.interfaces.Statement
import io.github.duartqx.ddql.sql.Select

public fun stmt(block: () -> Statement): String = block().toStatementString()

public fun select(vararg columns: Any, block: (Select.() -> Unit)? = null): Statement {
  return block?.let { Select(*columns).apply(it) } ?: Select(*columns)
}
