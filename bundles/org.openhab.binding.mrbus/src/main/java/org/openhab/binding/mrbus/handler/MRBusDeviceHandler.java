/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mrbus.handler;

import static org.openhab.binding.mrbus.MRBusBindingConstants.DEVICE_ADDRESS;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.mrbus.internal.MRBusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MRBusDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Benedikt Patt - Initial contribution
 */
public abstract class MRBusDeviceHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(MRBusDeviceHandler.class);
    protected byte deviceAddress;
    protected MRBusGatewayHandler bridge;
    protected Map<ChannelUID, State> channelStates = new HashMap<ChannelUID, State>();
    protected Map<String, LocalDateTime> lastUpdates = new HashMap<String, LocalDateTime>();

    public MRBusDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        if (readConfiguration()) {
            updateStatus(ThingStatus.ONLINE);
            this.bridge.registerDevice(this);
        }
    }

    @Override
    public void dispose() {
        if (this.bridge != null) {
            this.bridge.unregisterDevice(this);
        }
    }

    @Override
    protected void updateState(ChannelUID channelUID, State state) {
        channelStates.put(channelUID, state);
        super.updateState(channelUID, state);
    }

    private boolean readConfiguration() {
        try {
            deviceAddress = ((java.math.BigDecimal) getThing().getConfiguration().getProperties().get(DEVICE_ADDRESS))
                    .byteValueExact();

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

            msg.receiver = this.deviceAddress;
            msg.command = command;
            msg.subCommand = subCommand;
            msg.payload = payload;

            return bridge.sendMessage(msg);
        } catch (Exception ex) {
            return false;
        }
    }

    protected boolean hasToUpdate(String name) {
        LocalDateTime lastUpdate;

        synchronized (lastUpdates) {
            lastUpdate = lastUpdates.get(name);
        }

        return (lastUpdate == null || Duration.between(lastUpdate, LocalDateTime.now()).getSeconds() >= 30);
    }

    protected void setLastUpdate(String name) {
        synchronized (lastUpdates) {
            lastUpdates.put(name, LocalDateTime.now());
        }
    }
}
