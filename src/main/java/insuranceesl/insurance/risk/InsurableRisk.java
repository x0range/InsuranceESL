/**
 * Created by Torsten Heinrich
 */
package insuranceesl.insurance.risk;

//ESL
//import org.economicsl.inventory.Item;
import org.economicsl.Agent;
//import org.economicsl.ContractHandlingAgent;
import insuranceesl.insurance.contract.InsuranceContract;
import insuranceesl.insurance.upstream.ContractHandlingAgent;

//JDistLib from: http://jdistlib.sourceforge.net/javadoc/
import net.sourceforge.jdistlib.generic.GenericDistribution;
import net.sourceforge.jdistlib.Exponential;
//import net.sourceforge.jdistlib.Uniform;
//import net.sourceforge.jdistlib.Normal;
//import net.sourceforge.jdistlib.LogNormal;
//import net.sourceforge.jdistlib.Poisson;
import net.sourceforge.jdistlib.evd.GeneralizedPareto;

import net.sourceforge.jdistlib.rng.MersenneTwister;
import net.sourceforge.jdistlib.rng.RandomEngine;

import java.lang.Math;
import java.util.HashMap;
import java.util.Map;

//TODO: this class should extend org.economicsl.inventory.Good or org.economicsl.inventory.Item
//TODO: random number generator draws the same numbers for different agents/risks (apparently for all calls within a certain time window). How to solve this?
//TODO: ... so either give every org.economicsl.agent their own seed or lock the resource
public class InsurableRisk {
	
    private String name = new String();
	private double value;
	private double runtime;
	private double endTime;
	private GenericDistribution eventDist;
	private GenericDistribution eventSizeDist;
	private RandomEngine randomE;
	private InsuranceContract insuranceContract = null;
	private ContractHandlingAgent owner;
	private Map<Long, Double> eventSchedule;
	private final boolean DEBUGGING = true;

	
	//TODO: overload the constructor in a more generic way?
	public InsurableRisk(String name, ContractHandlingAgent owner, GenericDistribution valueDist, double runtime, GenericDistribution eventDist, GenericDistribution eventSizeDist, long seed) {
		//TODO: A RandomEngine instance with new random seed must supplied to avoid duplicates. Unfortunately, the class level RE does not yet exist at this point.
		this("", owner, valueDist.random(new MersenneTwister(System.nanoTime())), runtime, eventDist, eventSizeDist, seed);
	}

	public InsurableRisk(String name, ContractHandlingAgent owner, double value, double runtime, GenericDistribution eventDist, GenericDistribution eventSizeDist, long seed) {
		this.name = name;
		this.owner = owner;
		this.value = value;
		this.runtime = runtime;
		this.endTime = owner.getTime() + this.runtime;
		this.eventDist = eventDist;
		this.eventSizeDist = eventSizeDist;
		this.randomE = new MersenneTwister(seed);
		this.eventSchedule = new HashMap<Long, Double>();
		System.out.println("Insurable Risk created; ID " + this.name + " owned by " + this.owner.getName());
	}
	
	public InsurableRisk(ContractHandlingAgent owner, double runtime, long seed) {
		/**
		 * setting default distributions: 
		 *    Power Law with x_min = 10.
		 *                   alpha = 3.
		 *               => PDF(x) = 200 * x^(-3)
		 *    Exponential with lambda = .03
		 *               => PDF(x) = 0.03 * e^(-0.03*x) 
		 *    Power Law with x_min = 10.
		 *                   alpha = 3.
		 *               => PDF(x) = 200 * x^(-3)
		 */
		this("", owner, new GeneralizedPareto(10., 10.*3., 1./3.), runtime, new Exponential(33.33), new GeneralizedPareto(10., 10.*3., 1./3.), seed);
	}

	public InsurableRisk(ContractHandlingAgent owner, long seed) {
		/**
		 *  default to:
		 *   runtime = 100 periods
		 */
		this(owner, 100, seed);
	}
	
    public String getName() {
		return this.name;
	}
	
	public double getTimeToNextEvent() {
		return(this.eventDist.random(this.randomE));
	}
	
	public double getSizeOfEvent () {
		return(this.eventSizeDist.random(this.randomE));
	}

	public ContractHandlingAgent getOwner() {
		return this.owner;
	}

	public InsuranceContract getInsuranceContract() {
		return this.insuranceContract;
	}
	
	//not used
	public double getRuntime() {
		return this.runtime;
	}
	
	//not used
	public double getEndTime() {
		return this.endTime;
	}
	
	//not used?
	public double getValue() {
		return this.value;
	}

	public void addInsurance(InsuranceContract ic) {
		this.insuranceContract = ic;
		this.createEventSchedule();
	}
	
	private void createEventSchedule() {
		double time = this.owner.getTime();
		
		//clear eventSchedule
		this.eventSchedule = new HashMap<Long, Double>();
		
		//repopulate eventSchedule
		while (true) {
			time += this.getTimeToNextEvent();
			if (time <= this.endTime) {
				double damage = this.getSizeOfEvent();
				Long scheduleTime = Math.round(Math.ceil(time));				
				this.eventSchedule.merge(scheduleTime, damage, Double::sum);
			} else {
				break;
			}
		}
		System.out.println(this.eventSchedule);
	}
	
	public void step() {
	    if (this.insuranceContract != null) {
			Long presentTime = (long) this.owner.getTime();
		    Double damage = this.eventSchedule.get(presentTime);
		    if (damage != null) {
				if (this.DEBUGGING) {
					System.out.println("DEBUG InsurableRisk: step(): insurance will be invoked");
				}
			    this.insuranceContract.invokeContract(damage);
			    this.eventSchedule.remove(presentTime);
			} else {
				if (this.DEBUGGING) {
					System.out.print("DEBUG InsurableRisk: step(): NO CLAIMS;  eventSchedule: ");
					System.out.println(this.eventSchedule + " presentTime: " + presentTime + " damage: " + this.eventSchedule.get(presentTime));
				}
			}
		}
	}
}	
