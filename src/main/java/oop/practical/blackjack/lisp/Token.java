package oop.practical.blackjack.lisp;

record Token(
    Type type,
    String value
) {

    enum Type {
        NUMBER,
        IDENTIFIER,
        OPERATOR,
    }

}
