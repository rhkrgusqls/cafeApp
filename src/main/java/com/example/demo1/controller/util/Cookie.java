package com.example.demo1.controller.util;

public class Cookie {

    private static String sessionCookie = "";

    public static void setSessionCookie(String rawSetCookieHeader) {
        if (rawSetCookieHeader != null && !rawSetCookieHeader.isEmpty()) {
            sessionCookie = rawSetCookieHeader.split(";", 2)[0]; // JSESSIONID=...
        }
    }

    public static String getSessionCookie() {
        return sessionCookie;
    }
}
