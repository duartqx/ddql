package io.github.duartqx.ddql.sql

import io.github.duartqx.ddql.expr.As.`as`
import io.github.duartqx.ddql.expr.Eq.eq
import io.github.duartqx.ddql.expr.Neq.neq
import io.github.duartqx.ddql.sql
import kotlin.test.Test
import kotlin.test.assertFailsWith

class SqlInjectionTest {

  @Test
  fun `raw strings are blocked`() {
    assertFailsWith<IllegalArgumentException> {
      sql {
        select("id", "name" `as` "first_name")
        from("users")
        where(eq("id") or ("name" neq "'maria'"))
      }
    }

    assertFailsWith<IllegalArgumentException> { "name" neq "'maria'" }
  }

  @Test
  fun `simple injections are blocked`() {
    assertFailsWith<IllegalArgumentException> {
      sql {
        select("id", "name" `as` "first_name")
        from("users")
        where(eq("id") or ("name" neq "; delete from users"))
      }
    }
  }

  @Test
  fun `tautology injection is blocked`() {
    assertFailsWith<IllegalArgumentException> {
      sql {
        select("id", "name")
        from("users")
        where(eq("username") or ("password" neq "' OR '1'='1"))
      }
    }
  }

  @Test
  fun `stacked query injection is blocked`() {
    assertFailsWith<IllegalArgumentException> {
      sql {
        select("id", "name")
        from("users")
        where(eq("id") or ("name" neq "admin'; DROP TABLE users; --"))
      }
    }
  }

  @Test
  fun `union based injection is blocked`() {
    assertFailsWith<IllegalArgumentException> {
      sql {
        select("id", "name")
        from("users")
        where(eq("name") and "' UNION SELECT credit_card, cvv FROM payments --")
      }
    }
  }

  @Test
  fun `comment truncation injection is blocked`() {
    assertFailsWith<IllegalArgumentException> {
      sql {
        select("id", "name")
        from("users")
        where("name" neq "admin'--")
      }
    }
  }
}
