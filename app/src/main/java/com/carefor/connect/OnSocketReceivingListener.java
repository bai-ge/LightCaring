package com.carefor.connect;

/**
 * Created by baige on 2018/3/22.
 */

public interface OnSocketReceivingListener {
    void onReceivePacketBegin(BaseConnector connector, SocketPacket packet);
    void onReceivePacketEnd(BaseConnector connector, SocketPacket packet);
    void onReceivePacketCancel(BaseConnector connector, SocketPacket packet);
    void onReceivingPacketInProgress(BaseConnector connector, SocketPacket packet, float progress, int receivedLength);

    class SimpleOnSocketReceivingListener implements OnSocketReceivingListener{
        @Override
        public void onReceivePacketBegin(BaseConnector connector, SocketPacket packet) {

        }

        @Override
        public void onReceivePacketEnd(BaseConnector connector, SocketPacket packet) {

        }

        @Override
        public void onReceivePacketCancel(BaseConnector connector, SocketPacket packet) {

        }

        @Override
        public void onReceivingPacketInProgress(BaseConnector connector, SocketPacket packet, float progress, int receivedLength) {

        }
    }
}
