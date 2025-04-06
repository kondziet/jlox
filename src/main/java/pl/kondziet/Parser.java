package pl.kondziet;

import java.util.ArrayList;
import java.util.List;

import static pl.kondziet.Expression.*;
import static pl.kondziet.Statement.*;
import static pl.kondziet.TokenType.*;

public class Parser {

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Statement> parse() {
        List<Statement> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(statement());
        }
        return statements;
    }

    private Statement statement() {
        if (consumeIfAnyMatches(PRINT)) {
            return printStatement();
        }
        return expressionStatement();
    }

    private Statement printStatement() {
        Expression expression = expression();
        if (consumeIfAnyMatches(SEMICOLON)) {
            return new Print(expression);
        }
        throw error(peek(),  "Expect ';' after value.");
    }

    private Statement expressionStatement() {
        Expression expression = expression();
        if (consumeIfAnyMatches(SEMICOLON)) {
            return new Expr(expression);
        }
        throw error(peek(),  "Expect ';' after value.");
    }

    private Expression expression() {
        return equality();
    }

    private Expression equality() {
        Expression left = comparison();

        while (consumeIfAnyMatches(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expression right = comparison();

            left = new Binary(left, operator, right);
        }

        return left;
    }

    private Expression comparison() {
        Expression left = term();

        while (consumeIfAnyMatches(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expression right = term();

            left = new Binary(left, operator, right);
        }

        return left;
    }

    private Expression term() {
        Expression left = factor();

        while (consumeIfAnyMatches(MINUS, PLUS)) {
            Token operator = previous();
            Expression right = factor();

            left = new Binary(left, operator, right);
        }

        return left;
    }

    private Expression factor() {
        Expression left = unary();

        while (consumeIfAnyMatches(SLASH, STAR)) {
            Token operator = previous();
            Expression right = unary();

            left = new Binary(left, operator, right);
        }

        return left;
    }

    private Expression unary() {
        if (consumeIfAnyMatches(BANG, MINUS)) {
            return new Unary(previous(), unary());
        }

        return primary();
    }

    private Expression primary() {
        if (consumeIfAnyMatches(FALSE)) {
            return new Literal(false);
        }
        if (consumeIfAnyMatches(TRUE)) {
            return new Literal(true);
        }
        if (consumeIfAnyMatches(NIL)) {
            return new Literal(null);
        }
        if (consumeIfAnyMatches(NUMBER, STRING)) {
            return new Literal(previous().literal());
        }
        if (consumeIfAnyMatches(LEFT_PAREN)) {
            Expression left = expression();
            if (consumeIfAnyMatches(RIGHT_PAREN)) {
                return new Grouping(left);
            }
            throw error(peek(), "missing ')' after expression");
        }

        throw error(peek(), "expression expected");
    }

    private Token consume() {
        if (isAtEnd()) {
            return peek();
        }
        return tokens.get(current++);
    }

    private boolean consumeIfAnyMatches(TokenType... types) {
        for (TokenType type : types) {
            if (matches(type)) {
                consume();
                return true;
            }
        }
        return false;
    }

    private boolean matches(TokenType type) {
        return peek().type() == type;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean isAtEnd() {
        return peek().type() == EOF;
    }

    private ParseException error(Token token, String message) {
        Lox.error(token, message);
        return new ParseException();
    }

    private void synchronize() {
        consume();

        while (!isAtEnd()) {
            if (previous().type() == SEMICOLON) return;

            switch (peek().type()) {
                case CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> {
                    return;
                }
            }

            consume();
        }
    }
}
