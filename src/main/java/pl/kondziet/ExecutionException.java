package pl.kondziet;

public class ExecutionException extends RuntimeException {

    private final Token token;

    public ExecutionException(Token token, String message) {
        super(message);
        this.token = token;
    }

    public Token getToken() {
        return token;
    }
}
