/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.mrbus.handler;

import static org.openhab.binding.mrbus.MRBusBindingConstants.*;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.mrbus.internal.MRBusAnswerMessage;
import org.openhab.binding.mrbus.internal.MRBusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MRBusRollerShutterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Benedikt Patt - Initial contribution
 */
public class MRBusRollerShutterHandler extends MRBusDeviceHandler {

    private Logger logger = LoggerFactory.getLogger(MRBusRollerShutterHandler.class);
    private int currentPosition = 0;

    public MRBusRollerShutterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelID = channelUID.getId();

        logger.info("Handling command for Rollershutter {}", deviceAddress);

        switch (channelID) {
            case CHANNEL_LOCKED:
                if (command instanceof RefreshType) {
                    // Refresh wird direkt für alle Kanäle ausgeführt
                    MRBusAnswerMessage answer = bridge
                            .sendMessageGetAnswer(new MRBusMessage(deviceAddress, (byte) 0x03, (byte) 0x07));

                    if (answer.okay == true) {
                        if (answer.answer.payload[0] > 0) {
                            this.updateState(CHANNEL_LOCKED, OnOffType.ON);
                        } else {
                            this.updateState(CHANNEL_LOCKED, OnOffType.OFF);
                        }
                    }

                } else if (command.equals(OnOffType.ON)) {
                    bridge.sendMessage(new MRBusMessage(deviceAddress, (byte) 0x03, (byte) 0x08, new byte[] { 0x01 }));

                } else if (command.equals(OnOffType.OFF)) {
                    bridge.sendMessage(new MRBusMessage(deviceAddress, (byte) 0x03, (byte) 0x08, new byte[] { 0x00 }));
                }
                break;
            case CHANNEL_POSITION:
                if (command instanceof RefreshType) {
                    // Refresh wird direkt für alle Kanäle ausgeführt
                    MRBusAnswerMessage answer = bridge
                            .sendMessageGetAnswer(new MRBusMessage(deviceAddress, (byte) 0x03, (byte) 0x02));

                    if (answer.okay == true) {
                        currentPosition = answer.answer.payload[0];
                        this.updateState(CHANNEL_POSITION, new PercentType(currentPosition * 10));
                    }

                } else if (command instanceof PercentType) {
                    int newPosition;

                    PercentType val = (PercentType) command;
                    newPosition = val.intValue() / 10;

                    if (newPosition != currentPosition) {
                        bridge.sendMessage(new MRBusMessage(deviceAddress, (byte) 0x03, (byte) 0x01,
                                new byte[] { (byte) newPosition }));
                        currentPosition = newPosition;
                    }
                }
                break;
        }
    }

    @Override
    public void handleMessage(MRBusMessage message) {
        if (message.command == 0x03) {
            switch (message.subCommand) {
                case (byte) 100:
                    logger.debug("Update state command received");

                    currentPosition = message.payload[0];
                    this.updateState(CHANNEL_POSITION, new PercentType(currentPosition * 10));

                    if (message.payload[1] > 0) {
                        this.updateState(CHANNEL_LOCKED, OnOffType.ON);
                    } else {
                        this.updateState(CHANNEL_LOCKED, OnOffType.OFF);
                    }
                    break;
            }
        }
    }
}
