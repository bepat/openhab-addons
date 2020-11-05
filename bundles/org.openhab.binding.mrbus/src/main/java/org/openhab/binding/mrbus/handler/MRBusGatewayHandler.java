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

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.mrbus.internal.MRBusAnswerMessage;
import org.openhab.binding.mrbus.internal.MRBusGatewayJobHandler;
import org.openhab.binding.mrbus.internal.MRBusHelper;
import org.openhab.binding.mrbus.internal.MRBusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MRBusGatewayHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Benedikt Patt - Initial contribution
 */
public class MRBusGatewayHandler extends BaseBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(MRBusGatewayHandler.class);
    private short msgID = 0;
    private short busID = 0;
    private InetAddress gatewayIP;
    private int udpPort;
    private Map<Byte, MRBusDeviceHandler> registeredDevices = new HashMap<Byte, MRBusDeviceHandler>();
    private Map<Integer, MRBusGroupHandler> registeredGroups = new HashMap<Integer, MRBusGroupHandler>();

    // message handling
    private Thread receiveUdpThread;
    private MRBusMessage messageAnswer;
    private byte messageWaitID;
    private byte messageWaitSender;
    private Object messageWait = new Object();

    private MRBusGatewayJobHandler jobHandler;

    public MRBusGatewayHandler(Bridge bridge) {
        super(bridge);
        jobHandler = new MRBusGatewayJobHandler(this, this.scheduler);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.info("handleCommand is not implemented");

    }

    @Override
    public void initialize() {
        logger.info("initialize - begin");

        if (readConfiguration()) {
            updateStatus(ThingStatus.ONLINE);
        }

        receiveUdpThread = new Thread(new Runnable() {
            private DatagramSocket receiveSocket = null;
            private byte[] receiveData = new byte[128];
            private DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            @Override
            public void run() {
                boolean realError = false;

                try {
                    if (receiveSocket == null) {
                        logger.debug("Socket öffnen...");

                        try {
                            receiveSocket = new DatagramSocket(udpPort);
                        } catch (Exception ex) {
                            logger.error("Fehler beim Öffnen des Sockets: " + ex.getMessage());
                            realError = true;
                            throw ex;
                        }
                    }

                    while (true) {
                        receiveSocket.receive(receivePacket);
                        checkReceivedData(receivePacket.getData(), receivePacket.getLength());
                    }
                } catch (Exception ex) {
                    logger.warn("Fehler beim Lesen von Daten: " + ex.getMessage());
                } finally {
                    if (realError) {
                        if (receiveSocket != null && !receiveSocket.isClosed()) {
                            receiveSocket.close();
                            receiveSocket = null;
                        }
                    }
                }

                if (!realError) {
                    this.run();
                }
            }
        });

        logger.info("initialize - starting runnable");
        receiveUdpThread.start();

        logger.info("initialize - starting job handlers");
        jobHandler.Start();

        logger.info("initialize - done");
    }

    @Override
    public void dispose() {
        // TODO: stop receiveUdpThread
        jobHandler.Stop();
    }

    private void checkReceivedData(byte[] receivedBytes, int length) {
        logger.trace("UDP data received: " + MRBusHelper.bytesToHex(receivedBytes, length));

        if (length >= 10) {
            MRBusMessage message = new MRBusMessage();

            message.receiver = receivedBytes[0];
            message.sender = receivedBytes[1];
            message.length = receivedBytes[2];
            message.checksum = (short) (receivedBytes[3] + (short) (receivedBytes[4] << 8));
            message.type = receivedBytes[5];
            message.flags = receivedBytes[6];
            message.id = receivedBytes[7];
            message.command = receivedBytes[8];
            message.subCommand = receivedBytes[9];

            if (message.length > 10) {
                if (length > 10) {
                    message.payload = new byte[message.length - 10];

                    for (int index = 10; index < message.length; index++) {
                        message.payload[index - 10] = receivedBytes[index];
                    }
                } else {
                    logger.error("UDP data too short. Expected: %i, received: %i", message.length, length);
                }
            }

            logger.debug("Message received: sender: {}, receiver: {}, id: {}, flags: {}, cmd: {}, subCmd: {}",
                    (short) (message.sender & 0xff), (short) (message.receiver & 0xff), (short) (message.id & 0xff),
                    message.getFlagsString(), (short) (message.command & 0xff), (short) (message.subCommand & 0xff));
            if (message.payload != null && message.payload.length > 0) {
                logger.trace("payload: " + MRBusHelper.bytesToHex(message.payload, message.payload.length));
            }

            if ((message.flags & MR_FLAGS_ACK) == 0 && (message.flags & MR_FLAGS_NACK) == 0) {
                // kein ACK gesetzt -> normale Nachricht
                this.messageReceived(message);
            } else {
                // ACK gesetzt -> Antwort auf eine Nachricht von uns?
                synchronized (this.messageWait) {
                    // wird auf diese Nachricht gewartet?
                    if (this.messageWaitSender == message.sender && this.messageWaitID == message.id) {
                        this.messageAnswer = message;
                        this.messageWait.notify();
                    }
                }
            }
        } else {
            logger.debug("Data too short!");
        }
    }

    private boolean readConfiguration() {
        updateStatus(ThingStatus.OFFLINE);

        try {
            this.busID = ((java.math.BigDecimal) getThing().getConfiguration().getProperties().get(BUSID))
                    .shortValueExact();

        } catch (Exception ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bus address wrong");
            return false;

        }

        try {
            this.gatewayIP = InetAddress
                    .getByName(((String) getThing().getConfiguration().getProperties().get(IPADDRESS)));

        } catch (Exception ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "IP address wrong: " + ex.getMessage());
            return false;

        }

        try {
            this.udpPort = ((java.math.BigDecimal) (getThing().getConfiguration().getProperties().get(UDPPORT)))
                    .intValueExact();

        } catch (Exception ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "UDP port wrong: " + ex.getMessage());
            return false;

        }

        return true;
    }

    public void registerDevice(MRBusDeviceHandler device) {
        logger.info("registering device {}, address {}", device.getThing().getUID(), (short) device.deviceAddress);

        synchronized (registeredDevices) {
            if (registeredDevices.containsKey(device.deviceAddress)) {
                logger.error("device with  address {} already registered", (short) device.deviceAddress);
            } else {
                logger.info("device {} with address {} registered", device.getThing().getUID(),
                        (short) device.deviceAddress);
                registeredDevices.put(device.deviceAddress, device);
            }
        }
    }

    public void unregisterDevice(MRBusDeviceHandler device) {
        logger.info("unregistering device {} with address {}", device.getThing().getUID(),
                (short) device.deviceAddress);

        synchronized (registeredDevices) {
            if (registeredDevices.containsKey(device.deviceAddress)) {
                logger.info("device registered");
                registeredDevices.remove(device.deviceAddress);
            } else {
                logger.warn("device with addresss {} is not registered", (short) device.deviceAddress);
            }
        }
    }

    public void registerGroup(MRBusGroupHandler group) {
        logger.info("registering group {}, address {}", group.getThing().getUID(), group.groupAddress);

        synchronized (registeredGroups) {
            if (registeredGroups.containsKey(group.groupAddress)) {
                logger.error("group with  address {} already registered", group.groupAddress);
            } else {
                logger.info("device {} with address {} registered", group.getThing().getUID(), group.groupAddress);
                registeredGroups.put(group.groupAddress, group);
            }
        }
    }

    public void unregisterGroup(MRBusGroupHandler group) {
        logger.info("unregistering group {} with address {}", group.getThing().getUID(), group.groupAddress);

        synchronized (registeredGroups) {
            if (registeredGroups.containsKey(group.groupAddress)) {
                logger.info("device registered");
                registeredGroups.remove(group.groupAddress);
            } else {
                logger.warn("group with addresss {} is not registered", group.groupAddress);
            }
        }
    }

    private short getNextMsgID() {
        this.msgID++;

        if (this.msgID > 255) {
            this.msgID = 0;
        }

        return this.msgID;
    }

    private void messageReceived(MRBusMessage message) {
        if ((message.flags & MR_FLAGS_ACK) == 0) {
            synchronized (registeredDevices) {
                for (MRBusDeviceHandler device : registeredDevices.values()) {
                    if (device.deviceAddress == message.sender) {
                        logger.debug("relaying message sender: {} receiver: {} to device {}",
                                (short) (message.sender & 0xff), (short) (message.receiver & 0xff),
                                device.getThing().getUID());
                        device.handleMessage(message);
                        break;
                    }
                }
            }

            synchronized (registeredGroups) {
                if (message.command == MR_CMD_COMMON && message.subCommand == MR_SCMD_COMMON_GROUP_SET_STATE
                        && message.payload != null && message.payload.length >= 4) {
                    short address = ((short) ((short) (message.payload[0] << 8) + message.payload[1]));

                    for (MRBusGroupHandler group : registeredGroups.values()) {
                        if (group.groupAddress == address) {
                            // group address match
                            logger.debug("relaying message sender: {} to group {}", (short) (message.sender & 0xff),
                                    group.getThing().getUID());
                            group.handleMessage(message);
                            break;
                        }
                    }
                }
            }
        }
    }

    public boolean sendMessage(MRBusMessage message) {
        MRBusAnswerMessage answer = internalSendMessage(message);

        return answer.okay;
    }

    public MRBusAnswerMessage sendMessageGetAnswer(MRBusMessage message) {
        return internalSendMessage(message);
    }

    private synchronized MRBusAnswerMessage internalSendMessage(MRBusMessage message) {
        DatagramSocket socket = null;
        DatagramPacket packet = null;
        byte[] Data;
        int tryCount = 1;
        MRBusAnswerMessage retValue = new MRBusAnswerMessage();
        boolean retry = false;

        message.id = (byte) this.getNextMsgID();
        message.sender = (byte) this.busID;
        message.type = (byte) 'C';
        message.calculateChecksum();

        retValue.answer = null;

        // Nachricht wird 3 mal verschickt, falls keine Antwort kommt und ACK erwartet wird oder falls es sich um eine
        // Broadcast Nachricht handelt
        while (tryCount == 1 || (tryCount <= 3 && retry == true)) {
            logger.debug("Sending message #{} id: {}, to: {}, flags {}, cmd: {}, subCmd: {}", tryCount,
                    (short) (message.id & 0xff), (short) (message.receiver & 0xff), message.getFlagsString(),
                    (short) (message.command & 0xff), (short) (message.subCommand & 0xff));

            if (message.payload != null && message.payload.length > 0) {
                logger.trace("payload: " + MRBusHelper.bytesToHex(message.payload, message.payload.length));
            }

            Data = message.getBytes();

            try {
                socket = new DatagramSocket();
                packet = new DatagramPacket(Data, Data.length, this.gatewayIP, this.udpPort);
                this.messageAnswer = null;
                this.messageWaitID = message.id;
                this.messageWaitSender = message.receiver;
                socket.send(packet);

                if ((message.flags & MR_FLAGS_REQACK) > 0) {
                    // ACK erwartet
                    synchronized (this.messageWait) {
                        this.messageWait.wait(200);
                    }

                    if (this.messageAnswer != null) {
                        retValue.answer = this.messageAnswer;
                        if ((this.messageAnswer.flags & MR_FLAGS_ACK) > 0) {
                            // ACK erhalten
                            retValue.okay = true;
                            retry = false;
                        } else {
                            // kein ACK erhalten
                            logger.warn("Keine ACK erhalten!");
                            retValue.okay = false;
                            retry = false;
                        }
                    } else {
                        // keine Antwort
                        logger.warn("Keine Antwort auf Nachricht!");
                        retValue.okay = false;
                        retry = true;
                    }
                } else if (message.receiver == MR_BROADCAST_ADDRESS) {
                    // Broadcasts werden ebenfalls 3 mal gesendet
                    retValue.okay = true;
                    retry = true;
                } else {
                    // kein ACK erwartet
                    retValue.okay = true;
                    retry = false;
                }

            } catch (Exception ex) {
                logger.error("Fehler beim Senden der Nachricht: {}", ex.getMessage());
            } finally {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            }

            tryCount++;
        }

        return retValue;
    }

}
