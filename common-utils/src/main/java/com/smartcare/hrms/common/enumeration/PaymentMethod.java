package com.smartcare.hrms.common.enumeration;

public enum PaymentMethod {

    CASH("Cash"),
    CARD("Card"),
    UPI("UPI"),
    BANK_TRANSFER("Bank Transfer");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
