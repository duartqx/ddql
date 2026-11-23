package io.github.duartqx.ddql.sql.dml

import io.github.duartqx.ddql.expr.Eq.eq
import io.github.duartqx.ddql.fixtures.assertStatementEquals
import io.github.duartqx.ddql.insert
import io.github.duartqx.ddql.select
import kotlin.test.Test

class InsertTest {
  @Test
  fun `insert into table from values`() {
    assertStatementEquals(
        "insert into users (first_name, last_name) values (:first_name, :last_name)",
        insert {
          into("users")
          columns("first_name, last_name")
          values(":first_name", ":last_name")
        },
    )
  }

  @Test
  fun `insert into table from select`() {
    assertStatementEquals(
        "insert into users (first_name, last_name) select first_name, last_name from users where id = :id",
        insert {
          into("users")
          columns("first_name, last_name")
          from {
            select("first_name", "last_name") {
              from("users")
              where(eq("id"))
            }
          }
        },
    )
  }

  @Test
  fun `insert into table on conflict do nothing`() {
    assertStatementEquals(
        "insert into users (first_name, last_name) values (:first_name, :last_name) on conflict do nothing",
        insert {
          into("users")
          columns("first_name, last_name")
          values(":first_name", ":last_name")
          onConflict { doNothing() }
        },
    )
  }

  @Test
  fun `insert into table on conflict do update`() {
    assertStatementEquals(
        buildString {
          append("insert into users (first_name, last_name) ")
          append("values (:first_name, :last_name) ")
          append("on conflict do update set first_name = :first_name")
        },
        insert {
          into("users")
          columns("first_name, last_name")
          values(":first_name", ":last_name")
          onConflict { doUpdate { set("first_name", ":first_name") } }
        },
    )
  }

  @Test
  fun `insert into table on conflict do update from excluded`() {
    assertStatementEquals(
        buildString {
          append("insert into users (first_name, last_name) ")
          append("values (:first_name, :last_name) ")
          append("on conflict do update set first_name = excluded.first_name")
        },
        insert {
          into("users")
          columns("first_name, last_name")
          values(":first_name", ":last_name")
          onConflict { doUpdate { set("first_name", excluded("first_name")) } }
        },
    )
  }

  @Test
  fun `insert into table from values returning id`() {
    assertStatementEquals(
        buildString {
          append("insert into users (first_name, last_name) ")
          append("values (:first_name, :last_name) ")
          append("returning id")
        },
        insert {
          into("users")
          columns("first_name, last_name")
          values(":first_name", ":last_name")
          returning("id")
        },
    )
  }

  @Test
  fun `insert into table from values returning id as user_id`() {
    assertStatementEquals(
        buildString {
          append("insert into users (first_name, last_name) ")
          append("values (:first_name, :last_name) ")
          append("returning id as user_id")
        },
        insert {
          into("users")
          columns("first_name, last_name")
          values(":first_name", ":last_name")
          returning("id" to "user_id")
        },
    )
  }

  @Test
  fun `insert into table from values returning id and created_at`() {
    assertStatementEquals(
        buildString {
          append("insert into users (first_name, last_name) ")
          append("values (:first_name, :last_name) ")
          append("returning id, created_at")
        },
        insert {
          into("users")
          columns("first_name, last_name")
          values(":first_name", ":last_name")
          returning("id", "created_at")
        },
    )
  }
}
