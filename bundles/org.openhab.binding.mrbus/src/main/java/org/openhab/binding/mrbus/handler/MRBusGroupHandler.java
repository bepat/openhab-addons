/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mrbus.handler;

import static org.openhab.binding.mrbus.MRBusBindingConstants.GROUP_ADDRESS;
import static org.openhab.binding.mrbus.internal.MRBusProtocolConstants.MR_BROADCAST_ADDRESS;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.openhab.binding.mrbus.internal.MRBusMessage;

/**
 * The {@link MRBusGroupHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Benedikt Patt - Initial contribution
 */
public abstract class MRBusGroupHandler extends BaseThingHandler {

    // private Logger logger = LoggerFactory.getLogger(MRBusGroupHandler.class);
    protected Integer groupAddress;
    protected MRBusGatewayHandler bridge;

    public MRBusGroupHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        if (readConfiguration()) {
            updateStatus(ThingStatus.ONLINE);
            this.bridge.registerGroup(this);
        }
    }

    @Override
    public void dispose() {
        if (this.bridge != null) {
            this.bridge.unregisterGroup(this);
        }
    }

    private boolean readConfiguration() {
        try {
            groupAddress = ((java.math.BigDecimal) getThing().getConfiguration().getProperties().get(GROUP_ADDRESS))
                    .intValue();

        } catch (Exception ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Adresse falsch");
            return false;

        }

        try {
            bridge = (MRBusGatewayHandler) this.getBridge().getHandler();
        } catch (Exception ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Fehler beim Zuweisen der Bridge: " + ex.getMessage());
            return false;
        }

        return true;
    }

    public void handleMessage(MRBusMessage message) {
        // can be overridden by subclasses
    }

    protected boolean sendMessage(byte command, byte subCommand, byte payload[]) {
        try {
            MRBusMessage msg = new MRBusMessage();

            msg.receiver = MR_BROADCAST_ADDRESS;
            msg.command = command;
            msg.subCommand = subCommand;
            msg.payload = payload;

            return bridge.sendMessage(msg);
        } catch (Exception ex) {
            return false;
        }
    }
}
