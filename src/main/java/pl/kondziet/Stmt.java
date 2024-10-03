package pl.kondziet;

public sealed interface Stmt {

    record Print(Expr expr) implements Stmt {}

    record Expression(Expr expr) implements Stmt {}
}
