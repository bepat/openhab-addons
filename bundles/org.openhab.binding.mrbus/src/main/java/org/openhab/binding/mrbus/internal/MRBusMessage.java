/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.mrbus.internal;

import static org.openhab.binding.mrbus.internal.MRBusProtocolConstants.*;

/**
 * The {@link MRBusMessage} represents a deserialized MRBus mesage.
 *
 * @author Benedikt Patt - Initial contribution
 */
public class MRBusMessage {
    public byte sender;
    public byte receiver;
    public byte length;
    public short checksum;
    public byte type;
    public byte flags;
    public byte id;
    public byte command;
    public byte subCommand;
    public byte payload[];

    public MRBusMessage() {

    }

    public MRBusMessage(byte receiver, byte command, byte subCommand, byte payload[]) {
        this.receiver = receiver;
        this.command = command;
        this.subCommand = subCommand;
        this.payload = payload;

        if (receiver != MR_BROADCAST_ADDRESS) {
            this.flags = MR_FLAGS_REQACK;
        }
    }

    public MRBusMessage(byte receiver, byte command, byte subCommand) {
        this.receiver = receiver;
        this.command = command;
        this.subCommand = subCommand;
        this.payload = null;

        if (receiver != MR_BROADCAST_ADDRESS) {
            this.flags = MR_FLAGS_REQACK;
        }
    }

    public byte[] getBytes() {
        byte byteBuffer[] = new byte[10 + (payload != null ? payload.length : 0)];

        this.length = (byte) ((payload != null ? payload.length : 0) + 10);
        // this.flags = 1;

        byteBuffer[0] = (byte) (receiver & 0xff);
        byteBuffer[1] = (byte) (sender & 0xff);
        byteBuffer[2] = (byte) (length & 0xff);
        byteBuffer[3] = (byte) (checksum & 0xff);
        byteBuffer[4] = (byte) ((checksum >> 8) & 0xff);
        byteBuffer[5] = (byte) (type & 0xff);
        byteBuffer[6] = (byte) (flags & 0xff);
        byteBuffer[7] = (byte) (id & 0xff);
        byteBuffer[8] = (byte) (command & 0xff);
        byteBuffer[9] = (byte) (subCommand & 0xff);

        if (payload != null) {
            for (int index = 0; index < payload.length; index++) {
                byteBuffer[index + 10] = (byte) (payload[index] & 0xff);
            }
        }

        return byteBuffer;
    }

    public void calculateChecksum() {
        this.checksum = MRBusCRC.calculateCRC16(this.getBytesForChecksum());
    }

    public byte[] getBytesForChecksum() {
        byte byteBuffer[] = new byte[8 + (payload != null ? payload.length : 0)];

        this.length = (byte) ((payload != null ? payload.length : 0) + 10);
        // this.flags = 1;

        byteBuffer[0] = (byte) (receiver & 0xff);
        byteBuffer[1] = (byte) (sender & 0xff);
        byteBuffer[2] = (byte) (length & 0xff);
        byteBuffer[3] = (byte) (type & 0xff);
        byteBuffer[4] = (byte) (flags & 0xff);
        byteBuffer[5] = (byte) (id & 0xff);
        byteBuffer[6] = (byte) (command & 0xff);
        byteBuffer[7] = (byte) (subCommand & 0xff);

        if (payload != null) {
            for (int index = 0; index < payload.length; index++) {
                byteBuffer[index + 8] = (byte) (payload[index] & 0xff);
            }
        }

        return byteBuffer;
    }

    public String getFlagsString() {
        String ret = "";

        if ((this.flags & MR_FLAGS_REQACK) > 0) {
            ret += "R";
        }

        if ((this.flags & MR_FLAGS_ACK) > 0) {
            ret += "A";
        }

        if ((this.flags & MR_FLAGS_NACK) > 0) {
            ret += "N";
        }

        return ret;
    }
}
