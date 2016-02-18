/*
 *  Andre Cavalcante e Rafael Mendonca
 *  Copyright UFAM 2015-2016
 */
package eps;

import eps.ontology.EPSOntology;
import eps.ontology.Execute;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * Support services of MRA
 *
 * @author André
 */
public class MRAServices {

    private MRAServices() {
    }

    /**
     * Call this method to create a Initiator for an agent execute remotely
     *
     * @param thisAgent the agent that request the skill execution
     * @param mraInfo MRA that executes the skill 
     * @param st the template of the skill to be executed in the target agent
     * @return the return of executed skill;
     * @throws eps.SkillExecuteException If an execution error occurs
     */
    public static String executeRemoteSkill(Agent thisAgent, MRAInfo mraInfo, SkillTemplate st) throws SkillExecuteException {

        String result = "";

        Execute ex = new Execute();
        ex.setMRAInfo(mraInfo);
        ex.setSkillTemplate(st);
        Action act = new Action(new AID(mraInfo.getName(), false), ex);
        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
        request.setSender(thisAgent.getAID());
        request.addReceiver(new AID(mraInfo.getName(), false));
        request.setLanguage(new SLCodec().getName());
        request.setOntology(EPSOntology.EPSONTOLOGYNAME);
        try {
            thisAgent.getContentManager().fillContent(request, act);
            thisAgent.send(request);

            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.or(
                            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                            MessageTemplate.MatchPerformative(ACLMessage.FAILURE)),
                    MessageTemplate.MatchOntology(EPSOntology.EPSONTOLOGYNAME));
            ACLMessage msg = thisAgent.blockingReceive(mt, 30000);
            if (msg == null) {
                throw new SkillExecuteException("ExecuteRemoteSkill: Erro de timeout comunicando com AcHw.");
            } else {
                if (msg.getPerformative() == ACLMessage.FAILURE) {
                    throw new SkillExecuteException(msg.getContent());
                } else {
                    result = msg.getContent();
                }
            }
        } catch (Codec.CodecException | OntologyException ex1) {
            throw new SkillExecuteException("Error generating execute Skill. Exception: ", ex1);
        }

        return result;
    }
    
}
