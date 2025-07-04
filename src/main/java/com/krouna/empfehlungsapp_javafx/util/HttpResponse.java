package com.krouna.empfehlungsapp_javafx.util;

public class HttpResponse {

    private final int statusCode;
    private final String body;

    public HttpResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }




    public int getStatusCode() { // <-- Public Getter für statusCode
        return statusCode;
    }


    public String getBody() { // <-- Public Getter für body
        return body;
    }


    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }
}