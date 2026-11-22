package io.github.duartqx.ddql.fixtures

import io.github.duartqx.ddql.interfaces.Statement
import kotlin.test.assertEquals

fun assertStatementEquals(expected: String, actual: Statement) =
    assertEquals(expected, actual.toStatementString())
