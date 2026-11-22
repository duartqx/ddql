package io.github.duartqx.ddql.sql.dml

import io.github.duartqx.ddql.expr.Eq.eq
import io.github.duartqx.ddql.insert
import io.github.duartqx.ddql.select
import io.github.duartqx.ddql.stmt
import kotlin.test.Test
import kotlin.test.assertEquals

class InsertTest {
  @Test
  fun `insert into table from values`() {
    assertEquals(
        "insert into users (first_name, last_name) values (:first_name, :last_name)",
        stmt {
          insert {
            into("users", "first_name, last_name")
            values(listOf(":first_name", ":last_name"))
          }
        },
    )
  }

  @Test
  fun `insert into table from select`() {
    assertEquals(
        "insert into users (first_name, last_name) select first_name, last_name from users where id = :id",
        stmt {
          insert {
            into("users", "first_name, last_name")
            from {
              select("first_name", "last_name") {
                from("users")
                where(eq("id"))
              }
            }
          }
        },
    )
  }

  @Test
  fun `insert into table on conflict do nothing`() {
    assertEquals(
        "insert into users (first_name, last_name) values (:first_name, :last_name) on conflict do nothing",
        stmt {
          insert {
            into("users", "first_name, last_name")
            values(listOf(":first_name", ":last_name"))
            onConflict { doNothing() }
          }
        },
    )
  }

  @Test
  fun `insert into table on conflict do update`() {
    assertEquals(
        buildString {
          append("insert into users (first_name, last_name) ")
          append("values (:first_name, :last_name) ")
          append("on conflict do update set first_name = :first_name")
        },
        stmt {
          insert {
            into("users", "first_name, last_name")
            values(listOf(":first_name", ":last_name"))
            onConflict { doUpdate { set("first_name", ":first_name") } }
          }
        },
    )
  }

  @Test
  fun `insert into table on conflict do update from excluded`() {
    assertEquals(
        buildString {
          append("insert into users (first_name, last_name) ")
          append("values (:first_name, :last_name) ")
          append("on conflict do update set first_name = excluded.first_name")
        },
        stmt {
          insert {
            into("users", "first_name, last_name")
            values(listOf(":first_name", ":last_name"))
            onConflict { doUpdate { set("first_name", excluded("first_name")) } }
          }
        },
    )
  }
}
