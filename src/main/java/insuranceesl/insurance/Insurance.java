/**
 * Created by Torsten Heinrich
 */

package insuranceesl.insurance;

import java.util.ArrayList;
import java.util.List;

//ESL
import org.economicsl.Simulation;

import insuranceesl.insurance.agent.InsuranceCustomer;
import insuranceesl.insurance.agent.InsuranceFirm;
import insuranceesl.insurance.riskmodel.RiskModel;

import net.sourceforge.jdistlib.rng.MersenneTwister;


public class Insurance{

    private long seed;
	private long numberOfInsurers;
	private long numberOfRiskholders;
	private boolean scheduledEnd;
	private long scheduledEndTime;
	
	List<InsuranceFirm> insurers; 
	List<InsuranceCustomer> riskholders; 
	
	private Simulation simulation;
	
	public Insurance(long seed) {
		/**
		 *  Set the following default values:
		 *     numberOfInsurers    = 5
		 *     numberOfRiskholders = 100
		 *     scheduledEnd		   = true
		 *     scheduledEndTime    = 200
		 */
		this(seed, 5, 100, true, 200);
	}

	public Insurance(long seed, long numberOfInsurers, long numberOfRiskholders, boolean scheduledEnd, long scheduledEndTime) {
		//super(seed);
		this.seed = seed;
		this.numberOfInsurers = numberOfInsurers;
		this.numberOfRiskholders = numberOfRiskholders;
		this.scheduledEnd = scheduledEnd;
		this.scheduledEndTime = scheduledEndTime;
		this.simulation = new Simulation();
	}
	
	public void setup() {
        //super.setup();
		MersenneTwister seed = new MersenneTwister(5);
		
		this.insurers = new ArrayList<InsuranceFirm>();
		this.riskholders = new ArrayList<InsuranceCustomer>();
		
		for (int i = 1; i <= this.numberOfInsurers; i++) {
			RiskModel riskmodel = new RiskModel();
			InsuranceFirm a = new InsuranceFirm("Insurer " + i, this.simulation, riskmodel);
			try {
				a.getMainLedger().addGoods("cash", 100000.0, 1);
			} catch (Exception e) {
				e.printStackTrace();
			}
			this.insurers.add(a);
		}

		for (int i = 1; i <= this.numberOfRiskholders; i++) {
			//TODO: discuss whether this.insurers should be sent to InsuranceCustomer or should just be a class level variable here
			//TODO: insurers should become a global list since the set of insurers will eventually change over the course of the simulation
			//TODO: ... or perhaps not since the list is a pointer, as long as everyone has the same pointer, that is fine. but it should not be a local var of this.start()
			InsuranceCustomer a = new InsuranceCustomer("Risk Holder " + i, this.simulation, insurers, this.scheduledEnd, this.scheduledEndTime, seed);
			try {
				a.getMainLedger().addGoods("cash", 1000.0, 1);
			} catch (Exception e) {
				e.printStackTrace();
			}
			this.riskholders.add(a);
		}
	}

    // public void run(String[] args) {
	public void run() {
		// setup simulation
		this.setup();
		
		// cycle through time
		// call step for all agents for each time step
		while (true) {
			this.simulation.advance_time();
			this.insurers.forEach(ins -> ins.step());
			this.riskholders.forEach(rh -> rh.step());
			
			// breaking condition: stop simulation if scheduledEnd has passed
			if (this.scheduledEnd && (simulation.getTime() >= this.scheduledEndTime)) {
				break;
			}
		}			
		System.exit(0);
	}

    public static void main(String[] args) {
		int defaultSeed = 0;
		int seed;
		if (args.length > 0) {
			try {
				seed = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				seed = defaultSeed;
			}
		} else {
			seed = defaultSeed;
		}
		Insurance ins = new Insurance(seed);
		ins.run();
		System.exit(0);
	}
}
