/*
 * Copyright (c) Andre Cavalcante 2008-2015
 * All right reserved
 */
package eps;

import eps.ontology.Deregistry;
import eps.ontology.Registry;
import eps.ontology.Search;
import eps.ontology.EPSOntology;
import eps.ontology.GetAllMRAInfo;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.behaviours.OntologyServer;
import jade.domain.introspection.AMSSubscriber;
import jade.domain.introspection.DeadAgent;
import jade.domain.introspection.Event;
import jade.domain.introspection.IntrospectionVocabulary;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Yellow Page Agent (YPA)
 *
 * @author andre
 */
public class YPA extends Agent {

    public final static String YPA_AGENT_NAME = "ypa";

    private final Set<MRAInfo> table;

    public YPA() {
        table = Collections.synchronizedSet(new HashSet<MRAInfo>());
    }

    @Override
    protected void setup() {

        /**
         * Subscriber for realize deregister of agents.
         */
        AMSSubscriber myAMSSubscriber = new AMSSubscriber() {
            @Override
            protected void installHandlers(Map handlers) {
                // Associate an handler to dead-agent events
                EventHandler terminationsHandler = new EventHandler() {
                    public void handle(Event ev) {
                        DeadAgent da = (DeadAgent) ev;
                        System.out.println("Dead agent " + da.getAgent().getName());
                        deregistry(da.getAgent().getLocalName());
                    }
                };
                handlers.put(IntrospectionVocabulary.DEADAGENT, terminationsHandler);
            }
        };
        addBehaviour(myAMSSubscriber);

        addBehaviour(new OntologyServer(this, EPSOntology.instance(), ACLMessage.REQUEST, this));
    }

    @Override
    protected void takeDown() {
        super.takeDown();
    }

    public void serveRegistryRequest(Registry reg, ACLMessage request) {
        MRAInfo mraInfo;

        mraInfo = reg.getMRAInfo();
        boolean b = table.add(mraInfo);

        ACLMessage inform = request.createReply();
        inform.setPerformative(ACLMessage.INFORM);
        inform.setContent(b ? "OK" : "Already present. Not set");
        send(inform);

    }

    public void serveSearchRequest(Search search, ACLMessage request) {
        boolean ignoreProperties;
        SkillTemplate st;
        List<MRAInfo> mraInfoList;

        ignoreProperties = search.isIgnoreProperties();
        st = search.getSkillTemplate();
        mraInfoList = new ArrayList<>();
        for (MRAInfo mraInfo : table) {
            if (mraInfo.hasSkillTemplate(st, ignoreProperties)) {
                mraInfoList.add(mraInfo);
            }
        }

        search = new Search();
        search.setMraInfoArr(mraInfoList.toArray(new MRAInfo[0]));
        Action act = new Action();
        act.setAction(search);
        act.setActor(this.getAID());

        ACLMessage inform = request.createReply();
        try {
            inform.setPerformative(ACLMessage.INFORM);
            getContentManager().fillContent(inform, act);
            
            Debug.printMessageDebug("YPA INFORM content: " + inform.getContent(), Debug.DEBUG3);
        } catch (Codec.CodecException | OntologyException ex) {
            inform.setPerformative(ACLMessage.FAILURE);
            inform.setContent("Error generating inform. Exception: " + ex);
        }
        send(inform);
    }

    public void serveGetAllMRAInfoRequest(GetAllMRAInfo getAll, ACLMessage request) {

        getAll.setMRAInfoArr(table.toArray(new MRAInfo[0]));
        Action act = new Action();
        act.setAction(getAll);
        act.setActor(this.getAID());

        ACLMessage msg = request.createReply();
        try {
            msg.setPerformative(ACLMessage.INFORM);
            getContentManager().fillContent(msg, act);
        } catch (Codec.CodecException | OntologyException ex) {
            msg.setPerformative(ACLMessage.FAILURE);
            msg.setContent("Error getting skills from YPA. Exception: " + ex);
        }
        send(msg);
    }

    public void serveDeregistryRequest(Deregistry dereg, ACLMessage request) {
        deregistry(request.getSender().getLocalName());
        ACLMessage inform = request.createReply();
        inform.setPerformative(ACLMessage.INFORM);
        send(inform);
    }

    private void deregistry(String aid) {
        MRAInfo mraInfo;
        Iterator<MRAInfo> it = table.iterator();
        while (it.hasNext()) {
            mraInfo = it.next();
            if (mraInfo.getName().equals(aid)) {
                table.remove(mraInfo);
                return;
            }
        }
    }

}
