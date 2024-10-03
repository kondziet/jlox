package pl.kondziet;

public sealed interface Expr {

    record Binary(Expr left, Token operator, Expr right) implements Expr {}

    record Unary(Token operator, Expr right) implements Expr {}

    record Literal(Object value) implements Expr {}

    record Grouping(Expr expr) implements Expr {}
}
