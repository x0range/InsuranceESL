//package org.economicsl;
package insuranceesl.insurance.upstream;

import org.economicsl.Contract;
import org.economicsl.Simulation;

public abstract class StatedContract extends Contract {
    
    private State currentState;
	private String name;
	private int timeToOpen;
	protected Simulation simulation;
    
    public StatedContract(String name, Simulation simulation) {
		//super();	//parent has no constructor
        this.name = name;
   		currentState = State.NONE;
   		this.simulation = simulation;
   		this.timeToOpen = this.simulation.getTime() + 1;
    }
    
    public void setDefault() {
		this.currentState = State.DEFAULT;
	}
    
    public boolean isNotDefault() {
		boolean return_value = ((this.currentState == State.DEFAULT) ? false : true);
		return return_value;
	}

	//TODO: should not be public (since it is analogous to the same method in org.economicsl.Obligation where it is not public). But I cannot figure out a way to make it work otherwise.
    public boolean hasArrived() {
        return this.simulation.getTime() == timeToOpen;
    }

    public int getTime() {
		return this.simulation.getTime();
	}

    //REJECTED is a generic stated to be used by the simulation environment and needs to be there. But State can and should be overridden by classes inheriting from this one.
 	private enum State {
		NONE, REJECTED, DEFAULT
    }

}
