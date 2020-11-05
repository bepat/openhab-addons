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
 * The {@link MRBusLightControl2Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Benedikt Patt - Initial contribution
 */
public class MRBusLightControl2Handler extends MRBusDeviceHandler {

    private Logger logger = LoggerFactory.getLogger(MRBusLightControl2Handler.class);

    public MRBusLightControl2Handler(Thing thing) {
        super(thing);
    }

    @Override
    public synchronized void handleCommand(ChannelUID channelUID, Command command) {
        String channelID = channelUID.getId();
        short switchNo = 0;

        logger.info("Handling command for LightControl2 {}", deviceAddress);

        if (command instanceof RefreshType) {
            if (this.hasToUpdate("state")) {
                logger.debug("Getting new state...");
                MRBusAnswerMessage answer = bridge
                        .sendMessageGetAnswer(new MRBusMessage(deviceAddress, MR_CMD_LIGHT, MR_SCMD_LIGHT_GET_STATE));

                if (answer.okay == true) {
                    this.updateState(CHANNEL_SWITCH1, (answer.answer.payload[0] == 0 ? OnOffType.OFF : OnOffType.ON));
                    this.updateState(CHANNEL_SWITCH2, (answer.answer.payload[1] == 0 ? OnOffType.OFF : OnOffType.ON));
                    this.setLastUpdate("state");
                }
            } else {
                logger.debug("State already present!");
                this.updateState(channelID, channelStates.get(channelUID));
            }
        } else if (command.equals(OnOffType.ON) || command.equals(OnOffType.OFF)) {
            switch (channelID) {
                case CHANNEL_SWITCH1:
                    switchNo = 1;
                    break;
                case CHANNEL_SWITCH2:
                    switchNo = 2;
                    break;
            }

            if (switchNo > 0) {
                if (bridge
                        .sendMessage(new MRBusMessage(deviceAddress, MR_CMD_LIGHT, MR_SCMD_LIGHT_SET_STATE, new byte[] {
                                (byte) switchNo, (command.equals(OnOffType.ON) ? MR_SWITCH_ON : MR_SWITCH_OFF) }))) {
                    this.updateState(channelUID, (OnOffType) command);
                }
            }
        }
    }

    @Override
    public void handleMessage(MRBusMessage message) {
        if (message.command == MR_CMD_LIGHT) {
            switch (message.subCommand) {
                case MR_SCMD_LIGHT_STATUS_UPDATE:
                    logger.debug("Update state command received");
                    this.updateState(CHANNEL_SWITCH1, (message.payload[0] == 0 ? OnOffType.OFF : OnOffType.ON));
                    this.updateState(CHANNEL_SWITCH2, (message.payload[1] == 0 ? OnOffType.OFF : OnOffType.ON));
                    break;
            }
        }
    }
}
