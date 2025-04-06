package pl.kondziet;

public sealed interface Statement {

    record Expr(Expression expression) implements Statement {}

    record Print(Expression expression) implements Statement {}
}
