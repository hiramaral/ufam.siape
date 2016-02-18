/*
 * André, Hiram, Rafael
 * Copyright (c)UFAM 2015-2016
 */
package mainsiape;

import eps.YPA;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

/**
 *
 * @author hiramaral
 */
public class MainSiape {

    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        String platform;
        String host;
        int port;

        ContainerController cc;
        AgentController ac;

        platform = null;

        if (args.length == 1) {
            host = args[0];
        } else {
            host = "10.0.0.100";
        }

        port = 1099;
        
        Profile p = new ProfileImpl(host, port, platform, true);
        p.setParameter("gui", "true");

        jade.core.Runtime rt = jade.core.Runtime.instance();
        cc = rt.createMainContainer(p);

        Thread.sleep(2000);
        
        YPA ypa = new YPA();
        ac = cc.acceptNewAgent(YPA.YPA_AGENT_NAME, ypa);
        ac.start();
    }
}
