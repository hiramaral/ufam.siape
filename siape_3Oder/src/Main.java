/*
 *  Andre Cavalcante e Rafael Mendonca
 *  Copyright UFAM 2015-2016
 */

import eps.Debug;
import eps.YPA;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import test.siape.Anagram;

/**
 *
 * @author andre
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        ContainerController cc;
        AgentController ac;

        Debug.debugLevel = Debug.DEBUG2;

        jade.core.Runtime rt = jade.core.Runtime.instance();

        YPA ypa = new YPA();
        cc = rt.createAgentContainer(new ProfileImpl(false));
        ac = cc.acceptNewAgent(YPA.YPA_AGENT_NAME, ypa);
        ac.start();
        
        Thread.sleep(2000);
    
        Anagram prod = new Anagram("UFAM");
        cc = rt.createAgentContainer(new ProfileImpl(false));
        ac = cc.acceptNewAgent("Anagrama", prod);
        ac.start();
         

    }

}
