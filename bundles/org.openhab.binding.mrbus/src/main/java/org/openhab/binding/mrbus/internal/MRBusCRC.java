/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.mrbus.internal;

/**
 * The {@link MRBusCRC} is aa helper class for calculating crc.
 *
 * @author Benedikt Patt - Initial contribution
 */
public class MRBusCRC {
    public static short calculateCRC16(byte[] data) {
        int i;
        int bit;
        byte c;
        short crc = 0;

        for (int index = 0; index < data.length; index++) {
            c = data[index];

            for (i = 0x80; i > 0; i >>= 1) {
                bit = crc & (short) 0x8000;
                if ((c & (byte) i) != 0) {
                    bit = (bit != 0 ? 0 : 1);
                }
                crc <<= 1;
                if (bit != 0) {
                    crc ^= (short) 0xa001;
                }
            }
            crc &= (short) 0xffff;
        }

        return (short) (crc & (short) 0xffff);
    }

}
