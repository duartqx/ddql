package io.github.duartqx.ddql.sql

import io.github.duartqx.ddql.expr.As.`as`
import io.github.duartqx.ddql.expr.Eq.eq
import io.github.duartqx.ddql.expr.Gt.gt
import io.github.duartqx.ddql.expr.Neq.neq
import io.github.duartqx.ddql.fixtures.assertStatementEquals
import io.github.duartqx.ddql.select
import kotlin.test.Test

class SqlTest {
  @Test
  fun `select all columns`() {
    assertStatementEquals(
        "select * from users where id = :id",
        select {
          from("users")
          where(eq("id"))
        },
    )
  }

  @Test
  fun `select only id`() {
    assertStatementEquals(
        "select id from users where id = :id",
        select("id") {
          from("users")
          where(eq("id"))
        },
    )
  }

  @Test
  fun `select only id and name`() {
    assertStatementEquals(
        "select id, name from users where id = :id",
        select("id", "name") {
          from("users")
          where(eq("id"))
        },
    )
  }

  @Test
  fun `select only id and first_name alias filtering by id and neq name`() {
    assertStatementEquals(
        buildString {
          append("select id, name as first_name from users ")
          append("where (id = :id and name != :name)")
        },
        select("id", "name" `as` "first_name") {
          from("users")
          where(eq("id") and neq("name"))
        },
    )
  }

  @Test
  fun `select only id and first_name alias filtering by id or name neq`() {
    assertStatementEquals(
        buildString {
          append("select id, name as first_name from users ")
          append("where (id = :id or name != :name)")
        },
        select("id", "name" `as` "first_name") {
          from("users")
          where(eq("id") or neq("name"))
        },
    )
  }

  @Test
  fun `select all columns with join`() {
    assertStatementEquals(
        "select * from users as u join posts as p on p.user_id = u.id",
        select {
          from(("users" `as` "u"))
          join(inner("posts" `as` "p") on ("p.user_id" eq "u.id"))
        },
    )
  }

  @Test
  fun `select all columns join where`() {
    val expected = buildString {
      append("select * from users as u ")
      append("join posts as p on p.user_id = u.id ")
      append("where u.id = :uid")
    }

    assertStatementEquals(
        expected,
        select {
          from("users" `as` "u")
          join(inner("posts" `as` "p") on ("p.user_id" eq "u.id"))
          where("u.id" eq ":uid")
        },
    )

    assertStatementEquals(
        expected,
        select {
          from("users" `as` "u")
          join(inner("posts" `as` "p").on("p.user_id" eq "u.id"))
          where("u.id" eq ":uid")
        },
    )
  }

  @Test
  fun `simple cte select`() {
    assertStatementEquals(
        buildString {
          append("with u as (select count(*) as total, name from users group by name) ")
          append("select * from u where u.total > 100")
        },
        select {
          with(
              "u",
              select("count(*)" `as` "total", "name") {
                from("users")
                groupBy("name")
              },
          )
          from("u")
          where("u.total" gt 100)
        },
    )
  }

  @Test
  fun `double cte select`() {
    assertStatementEquals(
        buildString {
          append("with u as (select count(*) as total, name from users group by name), ")
          append("top_u as (select * from u where u.total > 100) ")
          append("select * from top_u")
        },
        select {
          with(
              "u",
              select("count(*)" `as` "total", "name") {
                from("users")
                groupBy("name")
              },
          )
          with(
              "top_u",
              select {
                from("u")
                where("u.total" gt 100)
              },
          )
          from("top_u")
        },
    )
  }

  @Test
  fun `double cte with join`() {
    assertStatementEquals(
        buildString {
          append("with active_users as (select id, name from users where active = true), ")
          append("user_posts as (select user_id, count(*) as total from posts group by user_id) ")
          append("select * from active_users as u ")
          append("join user_posts as p on p.user_id = u.id ")
          append("where p.total > 10")
        },
        select {
          with(
              "active_users",
              select("id", "name") {
                from("users")
                where("active" eq "true")
              },
          )
          with(
              "user_posts",
              select("user_id", "count(*)" `as` "total") {
                from("posts")
                groupBy("user_id")
              },
          )
          from("active_users" `as` "u")
          join(inner("user_posts" `as` "p") on ("p.user_id" eq "u.id"))
          where("p.total" gt 10)
        },
    )
  }

  @Test
  fun `simple select order by desc`() {
    assertStatementEquals(
        "select * from users order by created_at desc",
        select {
          from("users")
          orderBy { desc("created_at") }
        },
    )
  }

  @Test
  fun `simple select order by two fields`() {
    assertStatementEquals(
        "select * from users order by created_at desc, total asc",
        select {
          from("users")
          orderBy { desc("created_at") asc ("total") }
        },
    )
  }

  @Test
  fun `complex query with 3 ctes, inner join, left join and order by`() {
    assertStatementEquals(
        buildString {
          append("with cte1 as (select id from table1), ")
          append("cte2 as (select id from table2), ")
          append("cte3 as (select id from table3) ")
          append("select * from cte1 as c1 ")
          append("join cte2 as c2 on c2.id = c1.id ")
          append("left join cte3 as c3 on c3.id = c1.id ")
          append("order by created_at desc, total asc")
        },
        select {
          with("cte1", select("id") { from("table1") })
          with("cte2", select("id") { from("table2") })
          with("cte3", select("id") { from("table3") })
          from("cte1" `as` "c1")
          join(inner("cte2" `as` "c2") on ("c2.id" eq "c1.id"))
          join(left("cte3" `as` "c3") on ("c3.id" eq "c1.id"))
          orderBy { desc("created_at") asc ("total") }
        },
    )
  }

  @Test
  fun `select accepts case`() {
    assertStatementEquals(
        buildString {
          append("select name, ")
          append("case when total > 10 then :pass else :fail end ")
          append("from users")
        },
        select(
            "name",
            case {
              `when`("total" gt 10) then ":pass"
              `else`(":fail")
            },
        ) {
          from("users")
        },
    )
  }

  @Test
  fun `select exists`() {
    assertStatementEquals(
        "select exists (select 1 from users)",
        select(exists(select(1) { from("users") })),
    )
  }

  @Test
  fun `select not exists`() {
    assertStatementEquals(
        "select not exists (select 1 from users)",
        select(!exists(select(1) { from("users") })),
    )
  }
}
