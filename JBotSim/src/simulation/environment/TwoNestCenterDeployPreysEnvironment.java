package simulation.environment;

import java.util.LinkedList;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.util.Arguments;

public class TwoNestCenterDeployPreysEnvironment extends TwoNestSimpleForageEnvironment {

	private int numberOfPreysAvailable;
	private double rateOfNewPreyPerCycle;

	private LinkedList<Prey> foragedPreys = new LinkedList<Prey>();

	public TwoNestCenterDeployPreysEnvironment(Simulator simulator,
			Arguments arguments) {
		super(simulator, arguments);

		numberOfPreysAvailable = arguments.getArgumentAsIntOrSetDefault("numberOfPreysAvailable",3);
		rateOfNewPreyPerCycle = arguments.getArgumentAsIntOrSetDefault("rateOfNewPreyPerCycle",1);
	}
	
	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		deployPreys(simulator);
	}

	protected Vector2d newRandomPosition() {
		return new Vector2d(0, random.nextDouble() * 2 * forageLimit - forageLimit);
	}

	protected void reDeployPreys() {
		if (random.nextFloat() < rateOfNewPreyPerCycle && foragedPreys.size() > 0) {
			Prey newPrey = foragedPreys.poll();
			newPrey.setEnabled(true);
			newPrey.teleportTo(newRandomPosition());
		}
	}

	@Override
	protected void releasePrey(Prey prey) {
		super.releasePrey(prey);
		foragedPreys.add(prey);
	}

	@Override
	public void update(double time) {
		super.update(time);
		reDeployPreys();
	}

	@Override
	protected int getAmoutOfFood() {
		return numberOfPreysAvailable;
	}
}