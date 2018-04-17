package com.carefor.connect;

/**
 * Created by baige on 2018/3/23.
 */

public interface OnSocketServerListener {
    void onServerBeginListen(SocketServer socketServer, int port);

    void onServerStopListen(SocketServer socketServer, int port);

    void onClientConnected(SocketServer socketServer, ConnectedByTCP socketServerClient);

    void onClientDisconnected(SocketServer socketServer, ConnectedByTCP socketServerClient);

    class SimpleSocketServerDelegate implements OnSocketServerListener {
        @Override
        public void onServerBeginListen(SocketServer socketServer, int port) {

        }

        @Override
        public void onServerStopListen(SocketServer socketServer, int port) {

        }

        @Override
        public void onClientConnected(SocketServer socketServer, ConnectedByTCP socketServerClient) {

        }

        @Override
        public void onClientDisconnected(SocketServer socketServer, ConnectedByTCP socketServerClient) {

        }
    }
}
