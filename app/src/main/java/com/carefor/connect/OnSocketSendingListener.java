package com.carefor.connect;

/**
 * Created by baige on 2018/3/22.
 */

public interface OnSocketSendingListener {
    void onSendPacketBegin(BaseConnector connector, SocketPacket packet);
    void onSendPacketEnd(BaseConnector connector, SocketPacket packet);
    void onSendPacketCancel(BaseConnector connector, SocketPacket packet);
    /**
     * 发送进度回调
     * @param connector
     * @param packet 正在发送的packet
     * @param progress 0.0f-1.0f
     * @param sendedLength 已发送的字节数
     */
    void onSendingPacketInProgress(BaseConnector connector, SocketPacket packet, float progress, int sendedLength);

    class SimpleOnSocketSendingListener implements OnSocketSendingListener{
        @Override
        public void onSendPacketBegin(BaseConnector connector, SocketPacket packet) {

        }

        @Override
        public void onSendPacketEnd(BaseConnector connector, SocketPacket packet) {

        }

        @Override
        public void onSendPacketCancel(BaseConnector connector, SocketPacket packet) {

        }

        @Override
        public void onSendingPacketInProgress(BaseConnector connector, SocketPacket packet, float progress, int sendedLength) {

        }
    }
}
