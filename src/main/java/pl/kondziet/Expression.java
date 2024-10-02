package pl.kondziet;

public sealed interface Expression {

    record Binary(Expression left, Token operator, Expression right) implements Expression {}

    record Unary(Token operator, Expression right) implements Expression {}

    record Literal(Object value) implements Expression {}

    record Grouping(Expression expression) implements Expression {}
}
