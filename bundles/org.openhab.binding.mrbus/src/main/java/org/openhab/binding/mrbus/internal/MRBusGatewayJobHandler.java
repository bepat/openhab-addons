/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.mrbus.internal;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.mrbus.handler.MRBusGatewayHandler;
import org.openhab.binding.mrbus.internal.Jobs.TimeSyncJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MRBusGatewayJobHandler} handles periodically jobs for the gateway.
 *
 * @author Benedikt Patt - Initial contribution
 */
public class MRBusGatewayJobHandler {
    private Logger logger = LoggerFactory.getLogger(MRBusGatewayJobHandler.class);
    private MRBusGatewayHandler gateway;
    private ScheduledExecutorService scheduler;

    private ScheduledFuture<?> timeSyncJob;

    public MRBusGatewayJobHandler(MRBusGatewayHandler gateway, ScheduledExecutorService scheduler) {
        this.gateway = gateway;
        this.scheduler = scheduler;
    }

    public void Start() {
        logger.debug("starting...");

        timeSyncJob = scheduler.scheduleWithFixedDelay(new TimeSyncJob(gateway), 0, 1, TimeUnit.MINUTES);

        logger.debug("started!");
    }

    public void Stop() {
        logger.debug("stopping...");

        if (timeSyncJob != null) {
            timeSyncJob.cancel(true);
        }

        logger.debug("stopped!");
    }
}
