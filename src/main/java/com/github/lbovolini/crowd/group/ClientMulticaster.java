package com.github.lbovolini.crowd.group;

import java.net.InetSocketAddress;
import java.net.URL;

import static com.github.lbovolini.crowd.configuration.Config.*;

public class ClientMulticaster extends Multicaster {

    private final TimeScheduler timeScheduler = new TimeScheduler(this);

    public ClientMulticaster() {
        super(MULTICAST_CLIENT_PORT);
    }

    /**
     * Manipula somente mensagens recebidas do servidor.
     * Toda mensagem maior que 1 é uma mensagem enviada pelo servidor.
     * @param message
     * @param address
     */
    @Override
    protected void handle(String message, InetSocketAddress address) {
        timeScheduler.updateLastResponseTime();
        if (message.length() > 1) {
            ServerResponse serverResponse = ServerResponse.fromObject(message);
            handle(serverResponse);
        }
    }

    @Override
    protected void scheduler() {
        timeScheduler.start();
    }

    public void connect(URL[] codebase, URL libURL) {}

    public void update(URL[] codebase, URL libURL) {}

    public void reload(URL[] codebase, URL libURL) {}

    public void handle(ServerResponse response) {

        String type = response.getType();
        URL[] codebase = response.getCodebase();
        URL libURL = response.getLibURL();

        switch (type) {
            case CONNECT:
                connect(codebase, libURL);
                break;
            case UPDATE:
                update(codebase, libURL);
                break;
            case RELOAD:
                reload(codebase, libURL);
                break;
        }
    }

    public static void main(String[] args) {
        ClientMulticaster clientMulticaster = new ClientMulticaster() {
            @Override
            public void connect(URL[] codebase, URL libURL) {
                super.connect(codebase, libURL);
                System.out.println("CONNECT");
            }

            @Override
            public void update(URL[] codebase, URL libURL) {
                super.update(codebase, libURL);
            }

            @Override
            public void reload(URL[] codebase, URL libURL) {
                super.reload(codebase, libURL);
            }
        };
        clientMulticaster.start();
    }


}
