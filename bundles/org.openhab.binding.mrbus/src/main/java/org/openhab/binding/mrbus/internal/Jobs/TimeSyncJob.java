/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.mrbus.internal.Jobs;

import static org.openhab.binding.mrbus.internal.MRBusProtocolConstants.*;

import org.openhab.binding.mrbus.handler.MRBusGatewayHandler;
import org.openhab.binding.mrbus.internal.MRBusMessage;

/**
 * The {@link TimeSyncJob} is a periodically job for sending the current time on the bus.
 *
 * @author Benedikt Patt - Initial contribution
 */
public class TimeSyncJob implements Runnable {

    private MRBusGatewayHandler gateway;

    public TimeSyncJob(MRBusGatewayHandler gateway) {
        this.gateway = gateway;
    }

    @Override
    public void run() {
        // send current time
        int UnixTime = (int) (System.currentTimeMillis() / 1000L);
        MRBusMessage msg = new MRBusMessage(MR_BROADCAST_ADDRESS, MR_CMD_COMMON, MR_SCMD_COMMON_ENVIR_INFO,
                new byte[] { (byte) ((UnixTime >> 24) & 0xff), (byte) ((UnixTime >> 16) & 0xff),
                        (byte) ((UnixTime >> 8) & 0xff), (byte) ((UnixTime) & 0xff) });
        gateway.sendMessage(msg);
    }
}
