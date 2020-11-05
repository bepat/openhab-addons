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
 * The {@link MRBusAnswerMessage} is the result of a send mesage.
 *
 * @author Benedikt Patt - Initial contribution
 */
public class MRBusAnswerMessage {
    public MRBusMessage answer;
    public boolean okay;
}
