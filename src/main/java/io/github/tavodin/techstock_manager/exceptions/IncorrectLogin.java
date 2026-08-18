package io.github.tavodin.techstock_manager.exceptions;

public class IncorrectLogin extends RuntimeException {
    public IncorrectLogin(String message) {
        super(message);
    }
}
