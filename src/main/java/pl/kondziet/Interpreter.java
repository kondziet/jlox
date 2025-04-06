package pl.kondziet;

import java.util.List;
import java.util.Objects;

import static pl.kondziet.Expression.*;
import static pl.kondziet.Statement.*;


public class Interpreter {

    void interpret(List<Statement> statements) {
        try {
            statements.forEach(this::execute);
        } catch (ExecutionException e) {
            Lox.runtimeError(e);
        }
    }

    private void execute(Statement statement) {
        switch (statement) {
            case Expr e -> evaluate(e.expression());
            case Print p -> {
                Object value = evaluate(p.expression());
                System.out.println(stringify(value));
            }
        }
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

        if (left instanceof double l && right instanceof double r) {
            return switch (binary.operator().type()) {
                case MINUS -> l - r;
                case PLUS -> l + r;

                case SLASH -> l / r;
                case STAR -> l * r;

                case GREATER -> l > r;
                case GREATER_EQUAL -> l >= r;
                case LESS -> l < r;
                case LESS_EQUAL -> l <= r;

                case EQUAL_EQUAL -> Objects.equals(l, r);
                case BANG_EQUAL -> !Objects.equals(l, r);

                default -> throw new ExecutionException(binary.operator(), "unexpected operator in binary expression");
            };
        }

        if (left instanceof String l && right instanceof String r) {
            return switch (binary.operator().type()) {
                case PLUS -> l + r;

                case EQUAL_EQUAL -> Objects.equals(l, r);
                case BANG_EQUAL -> !Objects.equals(l, r);

                default -> throw new ExecutionException(binary.operator(), "unexpected operator in binary expression");
            };
        }

        throw new ExecutionException(binary.operator(), "operands must be two numbers or two strings");
    }

    private Object evaluateUnary(Unary unary) {
        Object right = evaluate(unary.right());

        return switch (unary.operator().type()) {
            case MINUS -> {
                if (right instanceof double r) {
                    yield -r;
                }
                throw new ExecutionException(unary.operator(), "Operand must be a number.");
            }
            case BANG -> !isTruthy(right);

            default -> throw new ExecutionException(unary.operator(), "unexpected operator in unary expression");
        };
    }

    private boolean isTruthy(Object object) {
        if (object == null) {
            return false;
        }
        if (object instanceof boolean b) {
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
