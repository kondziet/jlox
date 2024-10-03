package pl.kondziet;

import java.util.List;
import java.util.Objects;

import static pl.kondziet.Expr.*;
import static pl.kondziet.Stmt.*;
import static pl.kondziet.Main.runtimeError;

public class Interpreter {

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt stmt : statements) {
                execute(stmt);
            }
        } catch (ExecutionException e) {
            runtimeError(e);
        }
    }

    private void execute(Stmt stmt) {
        switch (stmt) {
            case Expression e -> executeExpression(e);
            case Print p -> executePrint(p);
        }
    }

    private void executePrint(Print print) {
        Object value = evaluate(print.expr());
        System.out.println(stringify(value));
    }

    private void executeExpression(Expression expression) {
        evaluate(expression.expr());
    }

    private Object evaluate(Expr expr) {
        return switch (expr) {
            case Binary b -> evaluateBinary(b);
            case Grouping g -> evaluate(g.expr());
            case Literal l -> l.value();
            case Unary u -> evaluateUnary(u);
        };
    }

    private Object evaluateBinary(Binary binary) {
        Object left = evaluate(binary.left());
        Object right = evaluate(binary.right());

        return switch (binary.operator().type()) {
            case MINUS -> {
                checkNumberOperands(binary.operator(), left, right);
                yield (double) left - (double) right;
            }
            case PLUS -> {
                if (left instanceof Double l && right instanceof Double r) {
                    yield l + r;
                }
                if (left instanceof String l && right instanceof String r) {
                    yield l + r;
                }
                throw new ExecutionException(binary.operator(), "operands must be two numbers or two strings");
            }
            case SLASH -> {
                checkNumberOperands(binary.operator(), left, right);
                yield (double) left / (double) right;
            }
            case STAR -> {
                checkNumberOperands(binary.operator(), left, right);
                yield (double) left * (double) right;
            }

            case GREATER -> {
                checkNumberOperands(binary.operator(), left, right);
                yield (double) left > (double) right;
            }
            case GREATER_EQUAL -> {
                checkNumberOperands(binary.operator(), left, right);
                yield (double) left >= (double) right;
            }
            case LESS -> {
                checkNumberOperands(binary.operator(), left, right);
                yield (double) left < (double) right;
            }
            case LESS_EQUAL -> {
                checkNumberOperands(binary.operator(), left, right);
                yield (double) left <= (double) right;
            }

            case BANG_EQUAL -> !Objects.equals(left, right);
            case EQUAL_EQUAL -> Objects.equals(left, right);

            default -> throw new ExecutionException(binary.operator(), "unexpected operator in binary expression");
        };
    }

    private Object evaluateUnary(Unary unary) {
        Object right = evaluate(unary.right());

        return switch (unary.operator().type()) {
            case MINUS -> {
                checkNumberOperand(unary.operator(), right);
                yield -(double) right;
            }
            case BANG -> !isTruthy(right);

            default -> throw new ExecutionException(unary.operator(), "unexpected operator in unary expression");
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

    // TODO: find a way to not use this method in each switch case
    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new ExecutionException(operator, "operand must be number");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new ExecutionException(operator, "operands must be numbers");
    }
}
