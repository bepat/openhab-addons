/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.mrbus.handler;

import static org.openhab.binding.mrbus.MRBusBindingConstants.CHANNEL_STATE;
import static org.openhab.binding.mrbus.internal.MRBusProtocolConstants.*;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.mrbus.internal.MRBusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MRBusSwitchGroupHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Benedikt Patt - Initial contribution
 */
public class MRBusSwitchGroupHandler extends MRBusGroupHandler {

    private Logger logger = LoggerFactory.getLogger(MRBusSwitchGroupHandler.class);

    public MRBusSwitchGroupHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelID = channelUID.getId();

        logger.info("Handling command for switch group {}", groupAddress);

        /*
         * if (command instanceof RefreshType) {
         * // Refresh wird direkt für alle Kanäle ausgeführt
         * MRBusAnswerMessage answer = bridge
         * .sendMessageGetAnswer(new MRBusMessage(deviceAddress, (byte) 0x05, (byte) 0x02));
         *
         * if (answer.okay == true) {
         * this.updateState(CHANNEL_SWITCH1, (answer.answer.payload[0] == 0 ? OnOffType.OFF : OnOffType.ON));
         * this.updateState(CHANNEL_SWITCH2, (answer.answer.payload[1] == 0 ? OnOffType.OFF : OnOffType.ON));
         * }
         * return;
         * }
         */

        if (channelID.equals(CHANNEL_STATE) && (command.equals(OnOffType.ON) || command.equals(OnOffType.OFF))) {
            boolean switchState = command.equals(OnOffType.ON);

            this.sendMessage(MR_CMD_COMMON, MR_SCMD_COMMON_GROUP_SET_STATE, new byte[] { (byte) (groupAddress >> 8),
                    (byte) (groupAddress & 0xff), MR_GROUPTYPE_SWITCH, (switchState ? MR_SWITCH_ON : MR_SWITCH_OFF) });
            this.updateState(channelUID, (switchState ? OnOffType.ON : OnOffType.OFF));
        }
    }

    @Override
    public void handleMessage(MRBusMessage message) {
        if (message.command == MR_CMD_COMMON) {
            switch (message.subCommand) {
                case MR_SCMD_COMMON_GROUP_SET_STATE:
                    logger.debug("Set group state command received");
                    if (message.payload[2] == MR_GROUPTYPE_SWITCH) {
                        this.updateState(CHANNEL_STATE,
                                (message.payload[3] == MR_SWITCH_OFF ? OnOffType.OFF : OnOffType.ON));
                    } else {
                        logger.warn("Wrong group type!");
                    }
                    break;
            }
        }
    }
}
