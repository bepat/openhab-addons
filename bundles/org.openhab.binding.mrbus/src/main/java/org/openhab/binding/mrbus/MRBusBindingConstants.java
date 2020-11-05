/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.mrbus;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link MRBusBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Benedikt Patt - Initial contribution
 */
public class MRBusBindingConstants {

    public static final String BINDING_ID = "mrbus";
    public static final String DEVICE_ADDRESS = "address";
    public static final String GROUP_ADDRESS = "groupaddress";
    public static final String BUSID = "busID";
    public static final String IPADDRESS = "ipAddress";
    public static final String UDPPORT = "udpPort";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_IPGATEWAY = new ThingTypeUID(BINDING_ID, "mrbusip");
    public final static ThingTypeUID THING_TYPE_ROLLERSHUTTER = new ThingTypeUID(BINDING_ID, "rollershutter");
    public final static ThingTypeUID THING_TYPE_LIGHTCONTROL2 = new ThingTypeUID(BINDING_ID, "lightcontrol2");
    public final static ThingTypeUID THING_TYPE_OVENCONTROL = new ThingTypeUID(BINDING_ID, "ovencontrol");
    public final static ThingTypeUID THING_TYPE_UNIVERSAL4I4O = new ThingTypeUID(BINDING_ID, "universal4i4o");
    public final static ThingTypeUID THING_TYPE_SWITCHGROUP = new ThingTypeUID(BINDING_ID, "switchgroup");

    // List of all Channel ids
    public final static String CHANNEL_POSITION = "position";
    public final static String CHANNEL_LOCKED = "locked";

    public final static String CHANNEL_STATE = "state";

    public final static String CHANNEL_SWITCH1 = "switch1";
    public final static String CHANNEL_SWITCH2 = "switch2";
    public final static String CHANNEL_SWITCH3 = "switch3";
    public final static String CHANNEL_SWITCH4 = "switch4";

    public final static String CHANNEL_ONOFF = "onoff";
    public final static String CHANNEL_POWER = "power";
    public final static String CHANNEL_FLOWLINETEMP = "flowlinetemp";
    public final static String CHANNEL_RETURNLINETEMP = "returnlinetemp";

}
