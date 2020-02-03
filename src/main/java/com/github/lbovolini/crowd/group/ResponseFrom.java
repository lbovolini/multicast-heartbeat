package com.github.lbovolini.crowd.group;

import java.net.InetSocketAddress;

public class ResponseFrom {
    private final String response;
    private final InetSocketAddress address;

    ResponseFrom(String  response, InetSocketAddress address) {
        this.response = response;
        this.address = address;
    }

    public String getResponse() {
        return response;
    }

    public InetSocketAddress getAddress() {
        return address;
    }
}