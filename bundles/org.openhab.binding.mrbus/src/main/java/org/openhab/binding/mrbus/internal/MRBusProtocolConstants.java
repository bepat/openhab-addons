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
 * The {@link MRBusProtocolConstants} contains all MRBus comands.
 *
 * @author Benedikt Patt - Initial contribution
 */
public class MRBusProtocolConstants {
    public static final byte MR_BROADCAST_ADDRESS = (byte) 0xff;

    public static final byte MR_FLAGS_REQACK = (byte) 0x01;
    public static final byte MR_FLAGS_ACK = (byte) 0x02;
    public static final byte MR_FLAGS_NACK = (byte) 0x04;

    public static final byte MR_VAL_OFF = 0;
    public static final byte MR_VAL_ON = 1;

    /* admin commands */
    public static final byte MR_CMD_ADMIN = 0;
    public static final byte MR_SCMD_ADMIN_RESET = 1; // resets the device
    public static final byte MR_SCMD_ADMIN_DEVICE_RUNNING = 7; // tell everyone that the device is running
    public static final byte MR_SCMD_ADMIN_SET_ADDRESS = 50; // sets the nodes address (first parameter)
    public static final byte MR_SCMD_ADMIN_GET_DEVICE_INFO = 11; // answer: devicetype, revision
    public static final byte MR_SCMD_ADMIN_GET_NAME = 12; // set device name
    public static final byte MR_SCMD_ADMIN_SET_NAME = 13; // get device name
    public static final byte MR_SCMD_ADMIN_DEBUG_MESSAGE = 14; // debug message, nothing to do

    /* common commands */
    public static final byte MR_CMD_COMMON = 1;
    public static final byte MR_SCMD_COMMON_ENVIR_INFO = 1; // TIME_H, TIME, TIME, TIME_L
    public static final byte MR_SCMD_COMMON_GROUP_SET_STATE = 10; // GROUP_H, GROUP_L, VALUE_TYPE, VALUE, [VALUE]...

    /* light commands */
    public static final byte MR_CMD_LIGHT = 5;
    public static final byte MR_SCMD_LIGHT_SET_STATE = 1; // CHANNEL, STATE
    public static final byte MR_SCMD_LIGHT_GET_STATE = 2; // CHANNEL_0_STATE, CHANNEL_1_STATE
    public static final byte MR_SCMD_LIGHT_STATUS_UPDATE = 100; // CHANNEL_0_STATE, CHANNEL_1_STATE

    /* oven commands */
    public static final byte MR_CMD_OVEN = 6;
    public static final byte MR_SCMD_OVEN_SET_ONOFF = 1; // 1: On/Off
    public static final byte MR_SCMD_OVEN_GET_STATE = 2; // 1: On/Off; 2: Power (20-100)
    public static final byte MR_SCMD_OVEN_SET_POWER = 3; // 1: Power (20-100)
    public static final byte MR_SCMD_OVEN_GET_TEMP = 4;// 1 ,2: (int16_t) flowline temperature; 3, 4 (int16_t)
                                                       // returnline temperature
    public static final byte MR_SCMD_OVEN_LEARN_SENSOR = 5; // 1: sensor index
    public static final byte MR_SCMD_OVEN_STATUS_UPDATE = 100; // 1: On/Off; 2: Power (20-100)
    public static final byte MR_SCMD_OVEN_TEMP_UPDATE = 101; // 1 ,2: (int16_t) flowline temperature; 3, 4 (int16_t)
                                                             // returnline temperature

    // group types
    public static final byte MR_GROUPTYPE_SWITCH = 1;

    public static final byte MR_SWITCH_OFF = 0;
    public static final byte MR_SWITCH_ON = 1;
}
