/**
 * Created by Torsten Heinrich
 */

package esl.agent;

import sim.engine.SimState;
import sim.engine.Steppable;

public abstract class MasonScheduledAgent extends Agent implements Steppable{

	public MasonScheduledAgent(String name, SimState state) {
		super(name);
		this.scheduleEvent(state);
	}

	public void step(SimState state) {
		this.scheduleEvent(state);
	}

	public void scheduleEvent(SimState state) {
		Double eventTime = this.scheduleNextEvent(state);
		if (eventTime != null) {
			state.schedule.scheduleOnce(eventTime, this);
		}		
	}
	
	public abstract Double scheduleNextEvent(SimState state);
}
