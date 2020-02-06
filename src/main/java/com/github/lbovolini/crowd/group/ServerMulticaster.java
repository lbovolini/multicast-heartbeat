package com.github.lbovolini.crowd.group;

import java.net.InetSocketAddress;

import static com.github.lbovolini.crowd.configuration.Config.*;

public class ServerMulticaster extends Multicaster {

    public ServerMulticaster() {
        super(MULTICAST_PORT);
    }

    @Override
    protected void scheduler() {
    }

    /**
     * handle mensagem do cliente
     * @param response
     * @param address
     */
    @Override
    protected void handle(String response, InetSocketAddress address) {

        if (response.length() > 1) {
            return;
        }
        if (DISCOVER.equals(response)) {
            join(address);
            response(CONNECT, address);
        }
        else if (HEARTBEAT.equals(response)) {
            if (isMember(address)) {
                response(HEARTBEAT, address);
            } else {
                join(address);
                response(CONNECT, address);
            }
        }
    }

    public static void main(String[] args) {
        ServerMulticaster serverMulticaster = new ServerMulticaster();
        serverMulticaster.start();
    }

}
