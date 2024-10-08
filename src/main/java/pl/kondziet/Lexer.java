package pl.kondziet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pl.kondziet.TokenType.*;
import static pl.kondziet.Main.error;

public class Lexer {

    private static final Map<String, TokenType> keywords = new HashMap<>();
    static {
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    }

    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0;
    private int current = 0;
    private int line = 1;

    public Lexer(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            scanToken();
        }
        tokens.add(new Token(EOF, "", null, line));

        return tokens;
    }

    private void scanToken() {
        start = current;
        char c = consume();

        switch (c) {
            case '(' -> addToken(LEFT_PAREN);
            case ')' -> addToken(RIGHT_PAREN);
            case '{' -> addToken(LEFT_BRACE);
            case '}' -> addToken(RIGHT_BRACE);
            case ',' -> addToken(COMMA);
            case '.' -> addToken(DOT);
            case '-' -> addToken(MINUS);
            case '+' -> addToken(PLUS);
            case ';' -> addToken(SEMICOLON);
            case '*' -> addToken(STAR);

            case '!' -> addToken(consumeIfMatches('=') ? BANG_EQUAL : BANG);
            case '=' -> addToken(consumeIfMatches('=') ? EQUAL_EQUAL : EQUAL);
            case '<' -> addToken(consumeIfMatches('=') ? LESS_EQUAL : LESS);
            case '>' -> addToken(consumeIfMatches('=') ? GREATER_EQUAL : GREATER);

            case '/' -> {
                if (consumeIfMatches('/')) {
                    comment();
                } else {
                    addToken(SLASH);
                }
            }

            case ' ', '\r', '\t' -> {}
            case '\n' -> line++;

            case '"' -> string();

            case char d when isDigit(d) -> number();
            case char a when isAlpha(a) -> identifier();

            default -> error(line, "unexpected character");
        }
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String lexeme = source.substring(start, current);
        tokens.add(new Token(type, lexeme, literal, line));
    }

    private char consume() {
        if (isAtEnd()) {
            return '\0';
        }
        return source.charAt(current++);
    }

    private boolean consumeIfMatches(char expected) {
        if (isAtEnd()) {
            return false;
        }
        if (source.charAt(current) != expected) {
            return false;
        }

        consume();
        return true;
    }

    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) {
            return '\0';
        }
        return source.charAt(current + 1);
    }

    private void comment() {
        while (peek() != '\n' && !isAtEnd()) {
            consume();
        }
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
            }
            consume();
        }

        if (isAtEnd()) {
            error(line, "unterminated string");
            return;
        }

        consume();

        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private void number() {
        while (isDigit(peek())) {
            consume();
        }

        if (peek() == '.' && isDigit(peekNext())) {
            consume();
            while (isDigit(peek())) {
                consume();
            }
        }

        Double value = Double.valueOf(source.substring(start, current));
        addToken(NUMBER, value);
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) {
            consume();
        }

        String text = source.substring(start, current);
        TokenType type = keywords.getOrDefault(text, IDENTIFIER);
        addToken(type);
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }
}
