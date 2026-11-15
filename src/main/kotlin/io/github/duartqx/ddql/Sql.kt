package io.github.duartqx.ddql

import io.github.duartqx.ddql.select.Sql

public fun sql(block: Sql.() -> Unit): String = Sql().apply(block).toStatementString()
