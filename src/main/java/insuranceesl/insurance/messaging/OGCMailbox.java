package insuranceesl.insurance.messaging;

import org.economicsl.obligations.ObligationsAndGoodsMailbox;
import org.economicsl.Agent;

import java.util.HashSet;
import java.util.stream.Collectors;
//import java.util.ArrayList;
//import java.util.List;
import java.util.Arrays;
import insuranceesl.insurance.upstream.StatedContract;


public class OGCMailbox extends ObligationsAndGoodsMailbox {
    private HashSet<StatedContract> contracts_unopened;
    private HashSet<StatedContract> contracts_outbox;
    private HashSet<StatedContract> contracts_inbox;
    private Agent owner;
    private final boolean DEBUGGING = false;
    
    public OGCMailbox (Agent owner) {
		super();
        this.contracts_unopened = new HashSet<>();
        this.contracts_outbox = new HashSet<>();
        this.contracts_inbox = new HashSet<>();
        
        this.owner = owner;
    }
    
    public void receiveContract(StatedContract contract) {

        contracts_unopened.add(contract);
        
        Agent first = contract.getAssetParty();
        Agent second = contract.getLiabilityParty();
        assert (this.owner == first || this.owner == second);
        
        Agent sender = ((first == this.owner) ? second : first);
        Agent recipient = ((first == sender) ? second : first);

        //System.out.println("Contract sent. " + contract.getName() + " offered by " + ...
        System.out.println("Contract sent; offered by " + sender.getName() + " to " + 
                            recipient.getName() + " in time step " + owner.getTime());
    }

    public void addToContractsOutbox(StatedContract contract) {
        contracts_outbox.add(contract);
    }
    
    public void removeFromContractsOutbox(HashSet<? extends StatedContract> contracts_tbr) {
        this.contracts_outbox.removeAll(contracts_tbr);
    }
    
    public void removeFromContractsOutbox(StatedContract contract) {
		HashSet<StatedContract> contracts_tbr = new HashSet<StatedContract>(Arrays.asList(contract));
        removeFromContractsOutbox(contracts_tbr);
    }
    
    public void removeFromContractsInbox(HashSet<? extends StatedContract> contracts_tbr) {
        this.contracts_inbox.removeAll(contracts_tbr);
    }

    public void removeFromContractsInbox(StatedContract contract) {
        HashSet<StatedContract> contracts_tbr = new HashSet<StatedContract>(Arrays.asList(contract));
        removeFromContractsInbox(contracts_tbr);
    }

    public void step() {
        
        super.step();
        
        // Move all messages in the obligation_unopened to the obligation_inbox
        contracts_inbox.addAll(
                contracts_unopened.stream()
                        .filter(StatedContract::hasArrived)
                        .collect(Collectors.toCollection(HashSet::new)));

        contracts_unopened.removeIf(StatedContract::hasArrived);
       	
       	if (this.DEBUGGING) {
			HashSet<? extends StatedContract> inboxContracts = this.getContracts_inbox();
			System.out.println("OCG inbox contracts " + inboxContracts.size());
		}
    }
    
    
    //not used
    public HashSet<StatedContract> getContracts_outbox() {
        return contracts_outbox;
    }

    //not used
    public HashSet<StatedContract> getContracts_inbox() {
        return contracts_inbox;
    }

}
