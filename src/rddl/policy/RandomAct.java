package rddl.policy;

import java.util.ArrayList;

import rddl.EvalException;
import rddl.RDDL.PVAR_INST_DEF;
import rddl.State;


public class RandomAct extends Policy{
	
	public ArrayList<ArrayList<Integer>> availableActBits = new ArrayList<>();
	RandomAct(){
		for(int c = 0; c < sumVars.size(); c ++) {
			availableActBits.add(new ArrayList<>());
			for(int k = 0; k < sumVars.get(c).size(); k ++) {
				availableActBits.get(c).add(sumVars.get(c).get(k));
			}
		}
	}
	@Override
	public ArrayList<PVAR_INST_DEF> getActions(State s) throws EvalException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public ArrayList<PVAR_INST_DEF> getNActions() throws EvalException {
		
		return null;
	}
}
