package com.carefor.connect;


/**
 * Created by baige on 2018/3/23.
 */

public interface OnDatagramSocketServerListener {
    void onServerStart(DatagramSocketServer server);
    void onServerClose(DatagramSocketServer server);
    void onServerReceivePacket(ConnectedByUDP connector, SocketPacket packet);
    void onClientConnected(ConnectedByUDP connector);
    void onClientDisconnected(ConnectedByUDP connector);

    class SimpleOnDatagramSocketServerListener implements OnDatagramSocketServerListener{
        @Override
        public void onServerStart(DatagramSocketServer server) {

        }

        @Override
        public void onServerClose(DatagramSocketServer server) {

        }

        @Override
        public void onServerReceivePacket(ConnectedByUDP connector, SocketPacket packet) {

        }

        @Override
        public void onClientConnected(ConnectedByUDP connector) {

        }

        @Override
        public void onClientDisconnected(ConnectedByUDP connector) {

        }
    }
}
