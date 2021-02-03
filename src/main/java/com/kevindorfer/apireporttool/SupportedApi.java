package com.kevindorfer.apireporttool;

public enum SupportedApi {
    FIND("Exact Match"),
    TEXT("Fuzzy Match");

    public final String title;

    SupportedApi(String title) {
        this.title = title;
    }
}
