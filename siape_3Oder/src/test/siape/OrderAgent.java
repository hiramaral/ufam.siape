/*
 *  Andre Cavalcante e Rafael Mendonca
 *  Copyright UFAM 2015-2016
 */
package test.siape;

import eps.Debug;
import eps.MRAInfo;
import eps.MRAServices;
import eps.SkillExecuteException;
import eps.SkillTemplate;
import eps.ontology.EPSOntology;
import jade.content.lang.sl.SLCodec;
import jade.core.ProfileImpl;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import javax.swing.SwingUtilities;

/**
 * Production order manager
 *
 * @author Rafael
 */
public class OrderAgent extends GuiAgent {

    public static final int EXITAGENT = 0;
    public static final int REFRESHSKILLS = 1;
    public static final int STARTORDERPRODUCE = 2;

    private final OrderGui frame;

    public OrderAgent(OrderGui frame) {
        this.frame = frame;
    }

    @Override
    protected void setup() {
        getContentManager().registerLanguage(new SLCodec());
        getContentManager().registerOntology(EPSOntology.instance());
    }

    @Override
    protected void takeDown() {
        super.takeDown();
    }
/**
 * Events 
 * @param ev 
 */
    @Override
    protected void onGuiEvent(GuiEvent ev) {
        int cmd = ev.getType();
        switch (cmd) {
            case EXITAGENT:
                doDelete();
                break;
            case REFRESHSKILLS:
                addBehaviour(refreshSkills());
                break;
            case STARTORDERPRODUCE:
                String[] anagramas = (String[]) ev.getParameter(0);
                startProduce(anagramas);
                break;
        }
    }
    /**
     * Method execute search of letters in YPA Services
     * @return 
     */

    private Behaviour refreshSkills() {
        return new OneShotBehaviour(this) {

            @Override
            public void action() {
                MRAInfo achwInfo;
                SkillTemplate st;

                achwInfo = new MRAInfo();
                achwInfo.setProperties(new String[0]);
                achwInfo.setSkills(new SkillTemplate[0]);
                achwInfo.setName("achw");

                st = new SkillTemplate("GetLetters", "void", new String[0]);
                String result;
                try {
                    result = MRAServices.executeRemoteSkill(myAgent, achwInfo, st);
                    final String[] letras = result.split(",");
                    SwingUtilities.invokeLater(() -> {
                        frame.refreshSkillsDone(letras);
                    });
                } catch (SkillExecuteException ex) {
                    Debug.printError("Execption: " + ex.toString());
                    SwingUtilities.invokeLater(() -> {
                        frame.refreshSkillsDone(new String[0]);
                    });
                }
            }
        };
    }
    /**
     * Starting produce of anagrams
     * @param anagramas 
     */

    private void startProduce(final String[] anagramas) {
        SequentialBehaviour seq = new SequentialBehaviour(this);
        for (int i = 0; i < anagramas.length; i++) {
            seq.addSubBehaviour(newAnagramBeh(anagramas[i], i));
            addBehaviour(seq);
        }
    }
    /**
     * Create agent anagrams and start method produce in agent
     * @param anagrama
     * @param i
     * @return 
     */

    private Behaviour newAnagramBeh(final String anagrama, final int i) {
        return new OneShotBehaviour(this) {
            @Override
            public void action() {
                ContainerController cc;
                jade.core.Runtime rt;

                rt = jade.core.Runtime.instance();
                cc = rt.createAgentContainer(new ProfileImpl(false));
                MRAInfo prodInfo;
                SkillTemplate st;
                AgentController ac;
                Anagram agent;
                try {

                    prodInfo = new MRAInfo();
                    prodInfo.setName("Anagrama" + i);
                    prodInfo.setProperties(new String[0]);
                    prodInfo.setSkills(new SkillTemplate[0]);

                    agent = new Anagram(anagrama);
                    ac = cc.acceptNewAgent(prodInfo.getName(), agent);
                    ac.start();

                  //Thread.sleep(100);
                    Thread.sleep(200);

                    st = new SkillTemplate("Produce");
                    MRAServices.executeRemoteSkill(myAgent, prodInfo, st);

                    SwingUtilities.invokeLater(() -> {
                        frame.anagramDone(anagrama, false);
                    });
                    
                    Thread.sleep(500);
                   // Thread.sleep(200);
                    cc.kill();

                } catch (StaleProxyException ex) {
                    System.out.println("Error creating Anagran Agent. Exception " + ex);
                    SwingUtilities.invokeLater(() -> {
                        frame.anagramDone(anagrama, true);
                    });
                } catch (InterruptedException ex) {
                    System.out.println("Exception " + ex);
                    SwingUtilities.invokeLater(() -> {
                        frame.anagramDone(anagrama, true);
                    });
                } catch (SkillExecuteException ex) {
                    System.out.println("Error executing remote skill. Exception " + ex);
                    SwingUtilities.invokeLater(() -> {
                        frame.anagramDone(anagrama, true);
                    });
                }
            }
        };
    }
}
