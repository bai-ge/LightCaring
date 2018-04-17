package com.carefor.connect;


import com.carefor.util.Tools;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SocketPacket
 * AndroidSocketClient <com.vilyever.vdsocketclient>
 * Created by vilyever on 2015/9/15.
 * Feature:
 */
public class SocketPacket {
    private final SocketPacket self = this;

    private static final AtomicInteger IDAtomic = new AtomicInteger();
    public static final byte SIGN_HEADER = 0x0A;
    public static final byte SIGN_MID = 0x0B;
    public static final byte SIGN_END = 0x0D;
    public static final byte[] heartBeatBuf = new byte[]{SIGN_END, SIGN_END, SIGN_END, SIGN_END};
    public static final byte[] disconnectedBuf = new byte[]{SIGN_END, SIGN_HEADER, SIGN_END, SIGN_HEADER};

    private final int ID;
    private byte[] headerBuf; //头部数据
    private byte[] contentBuf; //内容
    private byte[] allBuf;//全部，包括分割标记
    private boolean bPacket;//是否已经打包（发送之前打包）
    private boolean bHeartBeat;
    private boolean bDisconnected;

    public SocketPacket() {
        this.ID = IDAtomic.getAndIncrement();
        bHeartBeat = true;
    }

    public SocketPacket(byte[] buf, boolean isHeader) {
        this.ID = IDAtomic.getAndIncrement();
        bHeartBeat = false;
        if (isHeader) {
            headerBuf = Arrays.copyOf(buf, buf.length);
        } else {
            contentBuf = Arrays.copyOf(buf, buf.length);
        }
    }

    public SocketPacket(byte[] header, byte[] content) {
        this.ID = IDAtomic.getAndIncrement();
        bHeartBeat = false;
        headerBuf = Arrays.copyOf(header, header.length);
        contentBuf = Arrays.copyOf(content, content.length);
    }

    public int getID() {
        return ID;
    }

    public byte[] getHeaderBuf() {
        return headerBuf;
    }

    public void setHeaderBuf(byte[] headerBuf) {
        setHeartBeat(false);
        this.headerBuf = headerBuf;
    }

    public byte[] getContentBuf() {
        return contentBuf;
    }

    public void setContentBuf(byte[] contentBuf) {
        setHeartBeat(false);
        this.contentBuf = contentBuf;
    }

    public byte[] getAllBuf() {
        return allBuf;
    }

    public void setAllBuf(byte[] allBuf) {
        setPacket(true);
        this.allBuf = allBuf;
    }

    public boolean isHeartBeat() {
        return bHeartBeat;
    }
    public void setHeartBeat(boolean heartBeat) {
        this.bHeartBeat = heartBeat;
        if(heartBeat){
            setDisconnected(false);
        }
    }
    public boolean isDisconnected() {
        return bDisconnected;
    }

    public void setDisconnected(boolean disconnected) {
        this.bDisconnected = disconnected;
        if(disconnected){
            setHeartBeat(false);
        }
    }

    public boolean isPacket() {
        return bPacket;
    }

    public void setPacket(boolean bPacket) {
        this.bPacket = bPacket;
    }

    public SocketPacket packet() {
        if (isHeartBeat()) {
            allBuf = heartBeatBuf;
        }
        if (isDisconnected()) {
            allBuf = disconnectedBuf;
        } else {
            int headerSize = headerBuf == null ? 0 : headerBuf.length;
            int contentSize = contentBuf == null ? 0 : contentBuf.length;
            byte[] contentSizeBuf = lenToBuf(contentSize);
            if (headerSize > 255) {
                throw new IllegalArgumentException("the header len must less of 255");
            }
            ByteBuffer byteBuffer = ByteBuffer.allocate(3 + 1 + contentSizeBuf.length + headerSize + contentSize);
            byteBuffer.put(SIGN_HEADER);
            byteBuffer.put((byte) (headerSize & 0xFF));
            byteBuffer.put(contentSizeBuf);
            if (headerSize > 0) {
                byteBuffer.put(headerBuf);
            }
            byteBuffer.put(SIGN_MID);
            if (contentSize > 0) {
                byteBuffer.put(contentBuf);
            }
            byteBuffer.put(SIGN_END);
            allBuf = byteBuffer.array();
        }
        setPacket(true);
        return this;
    }

    //TODO 从流中解析出一个包，可能是心跳包，也可能是完整包
    public SocketPacket unPacket(InputStream input) throws IOException {
        if (input == null) {
            throw new IOException("InputStreamReader is closed");
        }

        return null;
    }

    // 从数组中解析出一个包，可能是心跳包，也可能是完整包
    public SocketPacket unPacket(byte[] buf) throws IOException {
        if (buf == null || buf.length <= 3) {
            return null;
        }
        ByteBuffer bytebuffer = ByteBuffer.wrap(buf);
        byte h = bytebuffer.get();
        if (h == SIGN_HEADER) {
            int headerSize = (bytebuffer.get() & 0x00FF);
            int contentSize = bufToLen(bytebuffer);
            if (headerSize > 0) {
                if (headerSize < bytebuffer.remaining()) {
                    byte[] header = new byte[headerSize];
                    bytebuffer.get(header);
                    setHeaderBuf(header);
                } else {
                    throw new IllegalArgumentException("It isn't a SocketPacket");
                }
            }
            if (bytebuffer.hasRemaining()) {
                h = bytebuffer.get();
            }
            if (h != SIGN_MID) {
                throw new IllegalArgumentException("It isn't a SocketPacket");
            }
            if (contentSize > 0) {
                if (contentSize < bytebuffer.remaining()) {
                    byte[] content = new byte[contentSize];
                    bytebuffer.get(content);
                    setContentBuf(content);
                } else {
                    throw new IllegalArgumentException("It isn't a SocketPacket");
                }
            }
            if (bytebuffer.hasRemaining()) {
                h = bytebuffer.get();
            }
            if (h != SIGN_END) {
                throw new IllegalArgumentException("It isn't a SocketPacket");
            }
        } else if (h == SIGN_END) {
            if (bytebuffer.remaining() >= 3) {
                byte[] remainBuf = new byte[3];
                bytebuffer.get(remainBuf);
                if (remainBuf[0] == SIGN_END
                        && remainBuf[1] == SIGN_END
                        && remainBuf[2] == SIGN_END) {
                    setHeartBeat(true);
                } else if (remainBuf[0] == SIGN_HEADER
                        && remainBuf[1] == SIGN_END
                        && remainBuf[2] == SIGN_HEADER) {
                    setHeartBeat(false);
                    setDisconnected(true);
                }else{
                    setHeartBeat(false);
                    setDisconnected(false);
                    throw new IllegalArgumentException("It isn't a SocketPacket");
                }
            } else {
                throw new IllegalArgumentException("It isn't a SocketPacket");
            }
        } else {
            throw new IllegalArgumentException("It isn't a SocketPacket");
        }
        return this;
    }

    public static byte[] lenToBuf(int len) {
        byte[] buf = null;
        if (len < 0) {
            throw new IllegalArgumentException("the argument len < 0");
        } else if (len <= 127) {
            buf = new byte[1];
            buf[0] = (byte) (len & 0xFF);
        } else {
            ArrayList<Byte> list = new ArrayList<>();
            list.add((byte) (len & 0x7f));
            len = len >> 7;
            while (len >= 127) {
                list.add((byte) ((len & 0x7f) | 0x80));
                len = len >> 7;
            }
            if (len > 0) {
                list.add((byte) ((len & 0x7f) | 0x80));
            }
            int size = list.size();
            buf = new byte[size];
            for (int i = size - 1; i >= 0; i--) {
                buf[size - 1 - i] = list.get(i);
            }
        }
        return buf;
    }

    public static int bufToLen(byte[] buf) {
        int len = 0;
        if (buf == null || buf.length == 0) {
            return 0;
        }
        for (int i = 0; i < buf.length; i++) {
            if ((buf[i] & 0x80) == 0) {
                len = len << 7;
                len = len | (buf[i] & 0x7f);
                break;
            }
            len = len << 7;
            len = len | (buf[i] & 0x7f);

        }
        return len;
    }

    public static int bufToLen(byte[] buf, int offset) {
        int len = 0;
        if (buf == null || buf.length == 0) {
            return 0;
        }
        for (int i = offset; i < buf.length; i++) {
            if ((buf[i] & 0x80) == 0) {
                len = len << 7;
                len = len | (buf[i] & 0x7f);
                break;
            }
            len = len << 7;
            len = len | (buf[i] & 0x7f);

        }
        return len;
    }

    public static int bufToLen(ByteBuffer buf) {
        int len = 0;
        if (buf == null || buf.remaining() == 0) {
            return 0;
        }
        while (buf.hasRemaining()) {
            byte b = buf.get();
            if ((b & 0x80) == 0) {
                len = len << 7;
                len = len | (b & 0x7f);
                break;
            }
            len = len << 7;
            len = len | (b & 0x7f);
        }
        return len;
    }

    public static int bufToLen(InputStream input) throws IOException {
        int len = 0;
        if (input == null) {
            throw new IOException("InputStreamReader is closed");
        }
        int data;
        while (-1 != (data = input.read())) {
            if ((data & 0x80) == 0) {
                len = len << 7;
                len = len | (data & 0x7f);
                break;
            }
            len = len << 7;
            len = len | (data & 0x7f);
        }
        return len;
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("{");
        if(isHeartBeat()){
            stringBuffer.append(" isHeartBeat:"+isHeartBeat());
        }else if(isDisconnected()){
            stringBuffer.append(" isDisconnected:"+isDisconnected());
        }else {
            if(getHeaderBuf() != null){
                stringBuffer.append(" header: size="+ getHeaderBuf().length + ", String="+ Tools.dataToString(getHeaderBuf(), Tools.DEFAULT_ENCODE)+"\n");
            }
            if(getContentBuf() != null){
                stringBuffer.append(" content: size="+ getContentBuf().length + ", String="+Tools.dataToString(getContentBuf(), Tools.DEFAULT_ENCODE)+"\n");
            }
        }
        stringBuffer.append(" }\n");
        return stringBuffer.toString();
    }

}