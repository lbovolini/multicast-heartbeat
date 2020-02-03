package com.github.lbovolini.crowd.group;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.lbovolini.crowd.configuration.Config.*;

public abstract class Multicaster {

    protected Selector selector;

    private final int port;
    private InetAddress group;
    private NetworkInterface networkInterface;

    protected DatagramChannel channel;
    private final Set<String> hosts = ConcurrentHashMap.newKeySet();
    private InetSocketAddress serverAddress;

    public Multicaster(int port) {
        this.port = port;
    }

    private void init(final DatagramChannel channel, final Selector selector) throws IOException {
        this.networkInterface = NetworkInterface.getByName(MULTICAST_INTERFACE_NAME);
        this.group = InetAddress.getByName(MULTICAST_IP);
        channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        // !TODO
        channel.bind(new InetSocketAddress("0.0.0.0", this.port));
        channel.setOption(StandardSocketOptions.IP_MULTICAST_IF, this.networkInterface);
        //channel.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, false);
        channel.configureBlocking(false);
        channel.join(this.group, this.networkInterface);
        channel.register(selector, SelectionKey.OP_READ);
    }

    public void start() {
        try (final DatagramChannel channel = DatagramChannel.open(StandardProtocolFamily.INET);
             final Selector selector = Selector.open()) {

            this.channel = channel;
            this.selector = selector;
            init(channel, selector);
            scheduler();

            while (true) {
                if (!selector.isOpen()) { break; }
                if (selector.select() == 0) { continue; }
                handleSelectionKeys(selector.selectedKeys());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected abstract void scheduler();

    private void read(SelectionKey selectionKey) throws IOException {

        DatagramChannel channel = (DatagramChannel) selectionKey.channel();
        ByteBuffer buffer = ByteBuffer.allocate(MULTICAST_BUFFER_SIZE);

        SocketAddress address = null;
        while (address == null) {
            address = channel.receive(buffer);
        }

        buffer.flip();
        String message = MulticastMessageUtils.getMessage(buffer);

        handle(message, (InetSocketAddress)address);
    }

    private void write(SelectionKey selectionKey) throws IOException {

        DatagramChannel channel = (DatagramChannel) selectionKey.channel();
        ResponseFrom responseFrom = (ResponseFrom) selectionKey.attachment();
        InetSocketAddress address = responseFrom.getAddress();

        //!TODO
        byte[] response = responseFrom.getResponse().getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.wrap(response);
        while (buffer.hasRemaining()) {
            channel.send(buffer, address);
        }
        buffer.clear();

        channel.register(selectionKey.selector(), SelectionKey.OP_READ);
    }

    protected boolean isMyself(InetSocketAddress address) {
        if (address.getAddress().getHostName().equals(HOST_NAME)) {
            return (address.getPort() == MULTICAST_PORT);
        }
        return false;
    }

    // !todo thread safe?
    private void wakeUp() {
        this.selector.wakeup();
    }

    protected void join(InetSocketAddress address) {
        hosts.add(address.toString());
    }

    protected boolean isMember(InetSocketAddress address) {
        return hosts.contains(address.toString());
    }

    private void handleSelectionKeys(final Set<SelectionKey> selectedKeys) throws IOException {
        Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

        while (keyIterator.hasNext()) {
            SelectionKey selectionKey = keyIterator.next();
            keyIterator.remove();

            if (!selectionKey.isValid()) { continue; }

            if (selectionKey.isReadable()) {
                read(selectionKey);
            } else if (selectionKey.isWritable()) {
                write(selectionKey);
            }
        }
    }

    private void setServerAddress(ServerResponse serverResponse) {
        this.serverAddress = serverResponse.getServerAddress();
    }

    /**
     * Envia mensagem somente para o servidor
     * @param message
     */
    public void send(String message) {
        responseFromTo(ResponseFactory.get(message), new InetSocketAddress(serverAddress.getAddress(), MULTICAST_PORT));
        wakeUp();
    }

    /**
     * Envia mensagem para todos participantes do grupo
     * @param message
     */
    public void sendAll(String message) {
        responseFromTo(ResponseFactory.get(message), new InetSocketAddress(MULTICAST_IP, MULTICAST_PORT));
        wakeUp();
    }

    protected void responseFromTo(String response, InetSocketAddress address) {
        ResponseFrom attach = new ResponseFrom(response, address);
        try {
            channel.register(selector, SelectionKey.OP_WRITE, attach);
        } catch (ClosedChannelException e) { e.printStackTrace(); }
    }

    public abstract void handle(ServerResponse serverResponse);

    /**
     * Se a resposta é maior do que 1, então foi enviada pelo servidor
     * @param response
     * @param address
     */
    protected void handle(String response, InetSocketAddress address) {
        if (response.length() > 1) {
            ServerResponse serverResponse = ServerResponse.fromObject(response);
            setServerAddress(serverResponse);
        }
    }



}
