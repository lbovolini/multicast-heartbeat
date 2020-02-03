package com.github.lbovolini.crowd.group;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

import static com.github.lbovolini.crowd.configuration.Config.*;

public class ServerMulticaster extends Multicaster {

    public ServerMulticaster() throws IOException {
        super(MULTICAST_PORT);
    }


    @Override
    protected void scheduler(DatagramChannel channel) {

    }

    @Override
    public void handle(ServerResponse serverResponse) {

    }

    @Override
    protected void handle(final DatagramChannel channel, String response, InetSocketAddress address) {

        System.out.println("A");
        if (response.length() > 1) {
            return;
        }
        if (DISCOVER.equals(response)) {
            join(address);
            responseFromTo(ResponseFactory.get(CONNECT), channel, address);
        }
        else if (HEARTBEAT.equals(response)) {
            if (isMember(address)) {
                responseFromTo(ResponseFactory.get(HEARTBEAT), channel, address);
            } else {
                join(address);
                responseFromTo(ResponseFactory.get(CONNECT), channel, address);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        ServerMulticaster serverMulticaster = new ServerMulticaster();
        serverMulticaster.start();
    }

}
