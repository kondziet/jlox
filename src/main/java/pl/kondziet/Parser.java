package pl.kondziet;

import java.util.ArrayList;
import java.util.List;

import static pl.kondziet.Expr.*;
import static pl.kondziet.Stmt.*;
import static pl.kondziet.TokenType.*;
import static pl.kondziet.Main.error;

public class Parser {

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> stmts = new ArrayList<>();
        while (!isAtEnd()) {
            stmts.add(statement());
        }

        return stmts;
    }

    private Stmt statement() {
        if (consumeIfAnyMatches(PRINT)) {
            return printStatement();
        }

        return expressionStatement();
    }

    private Stmt printStatement() {
        Expr value = expression();
        if (consumeIfAnyMatches(SEMICOLON)) {
            return new Print(value);
        }
        throw panic("expected ';' after expr");
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        if (consumeIfAnyMatches(SEMICOLON)) {
            return new Expression(expr);
        }

        throw panic("expected ';' after expr");
    }

    private Expr expression() {
        return equality();
    }

    private Expr equality() {
        Expr left = comparison();

        while (consumeIfAnyMatches(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();

            left = new Binary(left, operator, right);
        }

        return left;
    }

    private Expr comparison() {
        Expr left = term();

        while (consumeIfAnyMatches(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();

            left = new Binary(left, operator, right);
        }

        return left;
    }

    private Expr term() {
        Expr left = factor();

        while (consumeIfAnyMatches(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();

            left = new Binary(left, operator, right);
        }

        return left;
    }

    private Expr factor() {
        Expr left = unary();

        while (consumeIfAnyMatches(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();

            left = new Binary(left, operator, right);
        }

        return left;
    }

    private Expr unary() {
        if (consumeIfAnyMatches(BANG, MINUS)) {
            return new Unary(previous(), unary());
        }

        return primary();
    }

    private Expr primary() {
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
            Expr left = expression();
            if (consumeIfAnyMatches(RIGHT_PAREN)) {
                return new Grouping(left);
            }

            throw panic("missing ')' after expression");
        }

        throw panic("expression expected");
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

    private ParseException panic(String message) {
        error(peek(), message);
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
