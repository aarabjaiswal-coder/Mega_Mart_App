package com.megamart.exception;

public class WarehouseSyncException extends RuntimeException {
    private final Reason reason;

    public WarehouseSyncException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }

    public enum Reason {
        NOT_CONFIGURED,
        INVALID_KEY
    }
}
