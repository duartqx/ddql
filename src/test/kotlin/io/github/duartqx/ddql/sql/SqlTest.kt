package io.github.duartqx.ddql.sql

import io.github.duartqx.ddql.expr.As.`as`
import io.github.duartqx.ddql.expr.Eq.eq
import io.github.duartqx.ddql.expr.Gt.gt
import io.github.duartqx.ddql.expr.Neq.neq
import io.github.duartqx.ddql.select
import io.github.duartqx.ddql.stmt
import kotlin.test.Test
import kotlin.test.assertEquals

class SqlTest {
  @Test
  fun `select all columns`() {
    assertEquals(
        "select * from users where id = :id",
        stmt {
          select {
            from("users")
            where(eq("id"))
          }
        },
    )
  }

  @Test
  fun `select only id`() {
    assertEquals(
        "select id from users where id = :id",
        stmt {
          select("id") {
            from("users")
            where(eq("id"))
          }
        },
    )
  }

  @Test
  fun `select only id and name`() {
    assertEquals(
        "select id, name from users where id = :id",
        stmt {
          select("id", "name") {
            from("users")
            where(eq("id"))
          }
        },
    )
  }

  @Test
  fun `select only id and first_name alias filtering by id and neq name`() {
    assertEquals(
        buildString {
          append("select id, name as first_name from users ")
          append("where (id = :id and name != :name)")
        },
        stmt {
          select("id", "name" `as` "first_name") {
            from("users")
            where(eq("id") and neq("name"))
          }
        },
    )
  }

  @Test
  fun `select only id and first_name alias filtering by id or name neq`() {
    assertEquals(
        buildString {
          append("select id, name as first_name from users ")
          append("where (id = :id or name != :name)")
        },
        stmt {
          select("id", "name" `as` "first_name") {
            from("users")
            where(eq("id") or neq("name"))
          }
        },
    )
  }

  @Test
  fun `select all columns with join`() {
    assertEquals(
        "select * from users as u join posts as p on p.user_id = u.id",
        stmt {
          select {
            from(("users" `as` "u"))
            join(inner("posts" `as` "p") on ("p.user_id" eq "u.id"))
          }
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

    assertEquals(
        expected,
        stmt {
          select {
            from("users" `as` "u")
            join(inner("posts" `as` "p") on ("p.user_id" eq "u.id"))
            where("u.id" eq ":uid")
          }
        },
    )

    assertEquals(
        expected,
        stmt {
          select {
            from("users" `as` "u")
            join(inner("posts" `as` "p").on("p.user_id" eq "u.id"))
            where("u.id" eq ":uid")
          }
        },
    )
  }

  @Test
  fun `simple cte select`() {
    assertEquals(
        buildString {
          append("with u as (select count(*) as total, name from users group by name) ")
          append("select * from u where u.total > 100")
        },
        stmt {
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
          }
        },
    )
  }

  @Test
  fun `double cte select`() {
    assertEquals(
        buildString {
          append("with u as (select count(*) as total, name from users group by name), ")
          append("top_u as (select * from u where u.total > 100) ")
          append("select * from top_u")
        },
        stmt {
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
          }
        },
    )
  }

  @Test
  fun `double cte with join`() {
    assertEquals(
        buildString {
          append("with active_users as (select id, name from users where active = true), ")
          append("user_posts as (select user_id, count(*) as total from posts group by user_id) ")
          append("select * from active_users as u ")
          append("join user_posts as p on p.user_id = u.id ")
          append("where p.total > 10")
        },
        stmt {
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
          }
        },
    )
  }

  @Test
  fun `simple select order by desc`() {
    assertEquals(
        "select * from users order by created_at desc",
        stmt {
          select {
            from("users")
            orderBy { desc("created_at") }
          }
        },
    )
  }

  @Test
  fun `simple select order by two fields`() {
    assertEquals(
        "select * from users order by created_at desc, total asc",
        stmt {
          select {
            from("users")
            orderBy { desc("created_at") asc ("total") }
          }
        },
    )
  }

  @Test
  fun `complex query with 3 ctes, inner join, left join and order by`() {
    assertEquals(
        buildString {
          append("with cte1 as (select id from table1), ")
          append("cte2 as (select id from table2), ")
          append("cte3 as (select id from table3) ")
          append("select * from cte1 as c1 ")
          append("join cte2 as c2 on c2.id = c1.id ")
          append("left join cte3 as c3 on c3.id = c1.id ")
          append("order by created_at desc, total asc")
        },
        stmt {
          select {
            with("cte1", select("id") { from("table1") })
            with("cte2", select("id") { from("table2") })
            with("cte3", select("id") { from("table3") })
            from("cte1" `as` "c1")
            join(inner("cte2" `as` "c2") on ("c2.id" eq "c1.id"))
            join(left("cte3" `as` "c3") on ("c3.id" eq "c1.id"))
            orderBy { desc("created_at") asc ("total") }
          }
        },
    )
  }

  @Test
  fun `select accepts case`() {
    assertEquals(
        buildString {
          append("select name, ")
          append("case when total > 10 then :pass else :fail end ")
          append("from users")
        },
        stmt {
          select(
              "name",
              case {
                `when`("total" gt 10) then ":pass"
                `else`(":fail")
              },
          ) {
            from("users")
          }
        },
    )
  }

  @Test
  fun `select exists`() {
    assertEquals(
        "select exists (select 1 from users)",
        stmt {
          select(exists(select(1) { from("users") }))
        },
    )
  }

  @Test
  fun `select not exists`() {
    assertEquals(
        "select not exists (select 1 from users)",
        stmt {
          select(!exists(select(1) { from("users") }))
        },
    )
  }
}
