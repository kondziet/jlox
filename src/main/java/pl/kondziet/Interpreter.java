package pl.kondziet;

import java.util.Objects;

import static pl.kondziet.Expression.*;

public class Interpreter {

    void interpret(Expression expression) {
        Object evaluate = evaluate(expression);
        System.out.println(stringify(evaluate));
    }

    private Object evaluate(Expression expression) {
        return switch (expression) {
            case Binary b -> evaluateBinary(b);
            case Grouping g -> evaluate(g.expression());
            case Literal l -> l.value();
            case Unary u -> evaluateUnary(u);
        };
    }

    private Object evaluateBinary(Binary binary) {
        Object left = evaluate(binary.left());
        Object right = evaluate(binary.right());

        return switch (binary.operator().type()) {
            case MINUS -> (double) left - (double) right;
            case PLUS -> {
                if (left instanceof Double l && right instanceof Double r) {
                    yield l + r;
                }
                if (left instanceof String l && right instanceof String r) {
                    yield l + r;
                }
                // TODO: operands must be two numbers or two strings
                throw new IllegalStateException("Unexpected value: " + binary.operator().type());
            }
            case SLASH -> (double) left / (double) right;
            case STAR -> (double) left * (double) right;

            case GREATER -> (double) left > (double) right;
            case GREATER_EQUAL -> (double) left >= (double) right;
            case LESS -> (double) left < (double) right;
            case LESS_EQUAL -> (double) left <= (double) right;

            case BANG_EQUAL -> !Objects.equals(left, right);
            case EQUAL_EQUAL -> Objects.equals(left, right);

            default -> throw new IllegalStateException("Unexpected value: " + binary.operator().type());
        };
    }

    private Object evaluateUnary(Unary unary) {
        Object right = evaluate(unary.right());

        return switch (unary.operator().type()) {
            case MINUS -> -(double) right;
            case BANG -> !isTruthy(right);

            default -> throw new IllegalStateException("Unexpected value: " + unary.operator().type());
        };
    }

    private boolean isTruthy(Object object) {
        if (object == null) {
            return false;
        }
        if (object instanceof Boolean b) {
            return b;
        }
        return true;
    }

    private static String stringify(Object object) {
        if (object == null) {
            return "nil";
        }
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                return text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }
}
