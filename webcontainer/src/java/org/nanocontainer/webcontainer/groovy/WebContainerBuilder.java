/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *                                                                           *
 *****************************************************************************/

package org.nanocontainer.webcontainer.groovy;

import org.picocontainer.MutablePicoContainer;
import org.nanocontainer.webcontainer.PicoJettyServer;
import org.nanocontainer.script.groovy.buildernodes.AbstractBuilderNode;
import org.nanocontainer.NanoContainer;

import java.util.Map;

public class WebContainerBuilder extends AbstractBuilderNode {


    public WebContainerBuilder() {
        super("webContainer");
    }

    public Object createNewNode(Object current, Map map) {
        int port =0;
        if (map.containsKey("port")) {
            port = ((Integer) map.remove("port")).intValue();
        }
        String host;
        if (map.containsKey("host")) {
            host = (String) map.remove("host");
        } else {
            host = "localhost";
        }

        NanoContainer parentNano = (NanoContainer) current;
        MutablePicoContainer parentContainer = parentNano.getPico();

        if (port != 0) {
            PicoJettyServer server = new PicoJettyServer(host, port, parentContainer);
            parentContainer.addChildContainer(server);
            return new ServerBuilder(server, parentContainer);
        } else {
            PicoJettyServer server = new PicoJettyServer(parentContainer);
            parentContainer.addChildContainer(server);
            return new ServerBuilder(server, parentContainer);
        }
    }


}


