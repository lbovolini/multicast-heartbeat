package com.github.lbovolini.crowd.group;

import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.channels.DatagramChannel;

import static com.github.lbovolini.crowd.configuration.Config.*;

public class ClientMulticaster extends Multicaster {




    TimeScheduler timeScheduler = new TimeScheduler(this);


    public ClientMulticaster() {
        super(MULTICAST_CLIENT_PORT);
    }



    @Override
    protected void handle(final DatagramChannel channel, String response, InetSocketAddress address) {
        super.handle(channel, response, address);
        // !todo
        System.out.println("B");
        timeScheduler.updateLastResponseTime();
        // !todo
        if (response.length() > 1) {
            ServerResponse serverResponse = ServerResponse.fromObject(response);
            //Message message = Message.create(Byte.valueOf(serverResponse.getType()), response.getBytes(StandardCharsets.UTF_8));
            handle(serverResponse);
        }
    }


    @Override
    protected void scheduler(DatagramChannel channel) {
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
                System.out.println("CONNECT");
                break;
            case UPDATE:
                update(codebase, libURL);
                System.out.println("UPDATE");
                break;
            case RELOAD:
                reload(codebase, libURL);
                System.out.println("RELOAD");
                break;
        }
    }



    public static void main(String[] args) {
        ClientMulticaster clientMulticaster = new ClientMulticaster() {
            @Override
            public void connect(URL[] codebase, URL libURL) {
                super.connect(codebase, libURL);
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
