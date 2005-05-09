/*
 * Copyright 2004 by Lastminute.com, plc. 4 Buckingham Gate, London SW1E 6JP,
 * United Kingdom. All rights reserved.
 * 
 * This software is the confidential and proprietary information of lastminute.com plc
 * ("Confidential Information"). You shall not disclose such confidential information
 * and shall use it only in accordance with the terms of the license agreements you 
 * entered into with lastminute.com
 */
package org.nanocontainer.integrationkit;

import org.picocontainer.MutablePicoContainer;

/**
 * @author Mauro Talevi
 */
public interface ContainerPopulator {
    public void populateContainer(MutablePicoContainer container);
}
