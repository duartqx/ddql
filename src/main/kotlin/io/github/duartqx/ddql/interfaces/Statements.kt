package io.github.duartqx.ddql.interfaces

public interface Statement {
  public fun toStatementString(): String
}

public interface JoinStatement {
  public fun toJoinString(): String
}

public interface OrderStatement {
  public fun toOrderString(): String
}
