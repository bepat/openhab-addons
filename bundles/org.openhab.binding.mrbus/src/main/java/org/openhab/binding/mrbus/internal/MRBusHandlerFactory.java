/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mrbus.internal;

import static org.openhab.binding.mrbus.MRBusBindingConstants.*;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.mrbus.handler.MRBusGatewayHandler;
import org.openhab.binding.mrbus.handler.MRBusLightControl2Handler;
import org.openhab.binding.mrbus.handler.MRBusOvenControlHandler;
import org.openhab.binding.mrbus.handler.MRBusRollerShutterHandler;
import org.openhab.binding.mrbus.handler.MRBusSwitchGroupHandler;
import org.openhab.binding.mrbus.handler.MRBusUniversal4I4OHandler;

//import com.google.common.collect.Sets;

/**
 * The {@link MRBusHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Benedikt Patt - Initial contribution
 */
public class MRBusHandlerFactory extends BaseThingHandlerFactory {

    // private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = C
    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>();
    static {
        SUPPORTED_THING_TYPES.add(THING_TYPE_ROLLERSHUTTER);
        SUPPORTED_THING_TYPES.add(THING_TYPE_LIGHTCONTROL2);
        SUPPORTED_THING_TYPES.add(THING_TYPE_IPGATEWAY);
        SUPPORTED_THING_TYPES.add(THING_TYPE_OVENCONTROL);
        SUPPORTED_THING_TYPES.add(THING_TYPE_UNIVERSAL4I4O);
        SUPPORTED_THING_TYPES.add(THING_TYPE_SWITCHGROUP);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        // return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_IPGATEWAY)) {
            return new MRBusGatewayHandler((Bridge) thing);
        } else if (thingTypeUID.equals(THING_TYPE_LIGHTCONTROL2)) {
            return new MRBusLightControl2Handler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_ROLLERSHUTTER)) {
            return new MRBusRollerShutterHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_OVENCONTROL)) {
            return new MRBusOvenControlHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_UNIVERSAL4I4O)) {
            return new MRBusUniversal4I4OHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_SWITCHGROUP)) {
            return new MRBusSwitchGroupHandler(thing);
        } else {
            return null;
        }
    }
}
