package com.github.lbovolini.crowd.group;

import java.io.IOException;
import java.net.InetSocketAddress;

import static com.github.lbovolini.crowd.configuration.Config.*;

public class ServerMulticaster extends Multicaster {

    public ServerMulticaster() {
        super(MULTICAST_PORT);
    }

    @Override
    protected void scheduler() {
    }

    @Override
    public void handle(ServerResponse serverResponse) {
    }

    @Override
    protected void handle(String response, InetSocketAddress address) {

        if (response.length() > 1) {
            return;
        }
        if (DISCOVER.equals(response)) {
            join(address);
            responseFromTo(ResponseFactory.get(CONNECT), address);
        }
        else if (HEARTBEAT.equals(response)) {
            if (isMember(address)) {
                responseFromTo(ResponseFactory.get(HEARTBEAT), address);
            } else {
                join(address);
                responseFromTo(ResponseFactory.get(CONNECT), address);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        ServerMulticaster serverMulticaster = new ServerMulticaster();
        serverMulticaster.start();
    }

}
