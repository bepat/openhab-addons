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
import static org.openhab.binding.mrbus.internal.MRBusProtocolConstants.*;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.mrbus.internal.MRBusAnswerMessage;
import org.openhab.binding.mrbus.internal.MRBusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MRBusOvenControlHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Benedikt Patt - Initial contribution
 */
public class MRBusOvenControlHandler extends MRBusDeviceHandler {

    private Logger logger = LoggerFactory.getLogger(MRBusOvenControlHandler.class);
    private int currentPower = 0;

    public MRBusOvenControlHandler(Thing thing) {
        super(thing);
    }

    @Override
    public synchronized void handleCommand(ChannelUID channelUID, Command command) {
        String channelID = channelUID.getId();
        MRBusAnswerMessage answer;

        logger.info("Handling command for OvenControl {}", deviceAddress);

        if (command instanceof RefreshType) {
            switch (channelID) {
                case CHANNEL_POWER:
                case CHANNEL_ONOFF:
                    if (this.hasToUpdate("state")) {
                        logger.debug("Getting new state...");

                        answer = bridge.sendMessageGetAnswer(
                                new MRBusMessage(deviceAddress, MR_CMD_OVEN, MR_SCMD_OVEN_GET_STATE));

                        if (answer.okay == true) {
                            updateChannelsByStateMessage(answer.answer.payload);
                        } else {
                            this.updateState(CHANNEL_ONOFF, OnOffType.OFF);
                            this.updateState(CHANNEL_POWER, new DecimalType(2.5));
                        }
                    } else {
                        logger.debug("State already present!");
                        this.updateState(channelID, channelStates.get(channelUID));
                    }
                    break;

                case CHANNEL_FLOWLINETEMP:
                case CHANNEL_RETURNLINETEMP:
                    if (this.hasToUpdate("temp")) {

                        logger.debug("Getting new temp...");

                        answer = bridge.sendMessageGetAnswer(
                                new MRBusMessage(deviceAddress, MR_CMD_OVEN, MR_SCMD_OVEN_GET_TEMP));
                        if (answer.okay == true) {
                            updateChannelsByTempMessage(answer.answer.payload);
                        }
                    } else {
                        logger.debug("Temp already present!");
                        this.updateState(channelID, channelStates.get(channelUID));
                    }
                    break;
            }
        } else {
            switch (channelID) {
                case CHANNEL_ONOFF:
                    if (command.equals(OnOffType.ON)) {
                        bridge.sendMessage(new MRBusMessage(deviceAddress, MR_CMD_OVEN, MR_SCMD_OVEN_SET_ONOFF,
                                new byte[] { MR_VAL_ON }));

                    } else if (command.equals(OnOffType.OFF)) {
                        bridge.sendMessage(new MRBusMessage(deviceAddress, MR_CMD_OVEN, MR_SCMD_OVEN_SET_ONOFF,
                                new byte[] { MR_VAL_OFF }));
                    }
                    break;
                case CHANNEL_POWER:
                    if (command instanceof DecimalType) {
                        int newPower;

                        DecimalType val = (DecimalType) command;

                        newPower = val.toBigDecimal().multiply(new BigDecimal(10)).toBigInteger().intValue();

                        if (newPower > 100) {
                            newPower = 100;
                        } else if (newPower < 20) {
                            newPower = 20;
                        }

                        if (newPower != currentPower) {
                            bridge.sendMessage(new MRBusMessage(deviceAddress, MR_CMD_OVEN, MR_SCMD_OVEN_SET_POWER,
                                    new byte[] { (byte) newPower }));
                            currentPower = newPower;
                        }
                    }
                    break;
            }
        }
    }

    private void updateChannelsByTempMessage(byte[] payload) {
        this.updateState(CHANNEL_FLOWLINETEMP,
                new DecimalType(new BigDecimal((short) (((payload[1] & 0xFF) << 8) | (payload[0] & 0xFF)) / (double) 10)
                        .setScale(1, BigDecimal.ROUND_HALF_UP)));

        this.updateState(CHANNEL_RETURNLINETEMP,
                new DecimalType(new BigDecimal((short) (((payload[3] & 0xFF) << 8) | (payload[2] & 0xFF)) / (double) 10)
                        .setScale(1, BigDecimal.ROUND_HALF_UP)));

        this.setLastUpdate("temp");
    }

    private void updateChannelsByStateMessage(byte[] payload) {
        this.updateState(CHANNEL_ONOFF, (payload[0] == 0 ? OnOffType.OFF : OnOffType.ON));
        this.updateState(CHANNEL_POWER, new DecimalType(payload[1] / 10));
        currentPower = payload[1];
        this.setLastUpdate("state");
    }

    @Override
    public void handleMessage(MRBusMessage message) {
        if (message.command == MR_CMD_OVEN) {
            switch (message.subCommand) {
                case MR_SCMD_OVEN_STATUS_UPDATE:
                    logger.debug("Update state command received");
                    updateChannelsByStateMessage(message.payload);
                    break;

                case MR_SCMD_OVEN_TEMP_UPDATE:
                    logger.debug("Update temperature command received");
                    updateChannelsByTempMessage(message.payload);
                    break;
            }
        }
    }
}
