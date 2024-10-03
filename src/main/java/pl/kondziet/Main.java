package pl.kondziet;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static boolean hadError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFromFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFromFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        String source = new String(bytes, Charset.defaultCharset());
        run(source);
    }

    private static void runPrompt() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine();
            if (line.isBlank()) {
                break;
            }
            run(line);
        }
    }

    private static void run(String source) {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();

        Parser parser = new Parser(tokens);
        Expression expression = parser.parse();

        Interpreter interpreter = new Interpreter();
        interpreter.interpret(expression);
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    static void error(Token token, String message) {
        if (token.type() == TokenType.EOF) {
            report(token.line(), "end", message);
        } else {
            report(token.line(), token.lexeme(), message);
        }
    }

    private static void report(int line, String where, String message) {
        where = where.isBlank() ? "" : " at '%s'".formatted(where);
        System.err.printf("[line %d] error%s: %s\n", line, where, message);
        hadError = true;
    }
}