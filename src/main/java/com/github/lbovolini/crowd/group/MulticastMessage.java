package com.github.lbovolini.crowd.group;

public class MulticastMessage {

    private String message;

    public MulticastMessage(String message) {
        this.message = ResponseFactory.get(message);
    }

    public String getMessage() {
        return message;
    }

}
