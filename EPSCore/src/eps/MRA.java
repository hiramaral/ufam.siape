/*
 * Copyright (c) Andre Cavalcante 2008-2015
 * All right reserved
 */
package eps;

import eps.ontology.Execute;
import eps.ontology.EPSOntology;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.OntologyServer;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Class define one Mecatronic Agent. To create a mecatronic agent, create a
 * class extends MRA.
 *
 * @author andre
 */
public abstract class MRA extends Agent {

    public MRA() {
    }

    /**
     * The Mecatronic Agent MUST implements this method to return its
     * information
     *
     * @return the MRAInfo from this Mecatronic Agent.
     */
    protected abstract MRAInfo getMRAInfo();

    /**
     * The Mecatronic Agent MUST implements this method to return an array of
     * the truly skills of this MRA.
     *
     * @return the array of the MRA skills
     */
    protected abstract Skill[] getSkills();

    /**
     * This method executed after initialization father agent allowing son
     * initialization.
     */
    protected void init() {
    }

    /**
     * This method executed in OneShotBehaviour after initialization agent (end)
     * Automatic code executed.
     */
    protected void autorun() {
    }

    @Override
    protected void setup() {

        //registry the language and ontology
        getContentManager().registerLanguage(new SLCodec());
        getContentManager().registerOntology(EPSOntology.instance());

        //Registry the MRA in YPA
        try {
            YPAServices.registry(this, getMRAInfo());
        } catch (YPAException ex) {
            System.out.println("Registry in YPA return with error. Exception: " + ex);
        }

        //Execute the init()
        addBehaviour(new OneShotBehaviour(this) {
            @Override
            public void action() {
                init();
            }
        });

        //Behaviour to execute remotelly called skill
        addBehaviour(new OntologyServer(this, EPSOntology.instance(), ACLMessage.REQUEST, this));

        //Execute the autorun
        addBehaviour(new WakerBehaviour(this, 1000) {
            @Override
            public void onWake() {
                autorun();
            }
        });

    }

    @Override
    protected void takeDown() {
    }

    /**
     * Execute a remotely called skill in this machine.
     *
     * @param exec
     * @param request
     */
    public void serveExecuteRequest(Execute exec, ACLMessage request) {
        ACLMessage msg = request.createReply();
        msg.setSender(getAID());
        msg.setConversationId(request.getConversationId());
        try {
            ContentElement ce = getContentManager().extractContent(request);
            if (ce instanceof Action) {
                Execute ex = (Execute) ((Action) ce).getAction();
                SkillTemplate st = ex.getSkillTemplate();
                boolean b = false;
                for (Skill sk : getSkills()) {
                    if (st.equals((SkillBase) sk)) {
                        sk.setArgsValues(st.getArgsValues());
                        sk.execute();
                        msg.setPerformative(ACLMessage.INFORM);
                        msg.setContent(sk.getResult());
                        b = true;
                        break;
                    }
                }
                if (!b) {
                    msg.setPerformative(ACLMessage.FAILURE);
                    msg.setContent("Skill not found!!!");
                }
            }
        } catch (Codec.CodecException | OntologyException | SkillExecuteException ex) {
            msg.setPerformative(ACLMessage.FAILURE);
            msg.setContent(ex.toString());
        }
        send(msg);
    }
}
