package rddl.policy;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.util.CombinatoricsUtils;

import rddl.EvalException;
import rddl.Global;
import rddl.RDDL;
import rddl.RDDL.BOOL_EXPR;
import rddl.RDDL.INSTANCE;
import rddl.RDDL.LCONST;
import rddl.RDDL.LVAR;
import rddl.RDDL.OBJECT_TYPE_DEF;
import rddl.RDDL.PVARIABLE_DEF;
import rddl.RDDL.PVAR_EXPR;
import rddl.RDDL.PVAR_INST_DEF;
import rddl.RDDL.PVAR_NAME;
import rddl.RDDL.TYPE_NAME;
import rddl.State;
import rddl.TEState;
import rddl.TreeExp;
import sun.security.jca.GetInstance.Instance;

public class Y_F_Sample_Exp extends Policy {
	
	public Y_F_Sample_Exp () {
		super();
	}
	
	public Y_F_Sample_Exp(String instance_name) {
		super(instance_name);
		Global.ifLift = true;
		Global.ifRecordLift = true;
		searchDepth = 5;
		expectMaxVarDepth = 50;
		ifConformant = false;
		maxNumObs = 10;
	}
	
	boolean ifConverge = false;
	final double convergeNorm = 0.01;
	//if we only do certain nubber of pdates
	//we cannot see the overall trends of ratio action seen/updates
	//so we use this to adjust our estimate
	//so action seen at each step = actionSeen / staCounter * actionEstimateAdj
	final double actionEstimateAdj = 1;
	
	//double alpha = 0.00001;
	//convergence threashold
	//this is just init value. it will be adjusted by another par
	double ConvergeT = 0.0000001;
	
	//the portion of time spent on sampling final actions, given the marginal prob of each bit
	final double SampleTime = 0.2;
	//how many times do we wanna update each action bit
	int numOfIte = 200;
	
	//if trySeeing  > ratioOftrials * # possbile act, then set numOfIte = ratioOftrials * # possbile act
	// meaning that by estimation, we only wanna see a certain ratio of all actions
	//otherwise it is wasting time
	//how many updates to make to do the estimate
	Double ratioOfTrials = 0.3;
	//try certain number of updates
	final int tryUpdates = 5;
	//try seeing certain number of actions
	 int trySeeing = 5;
	//the depth of variables that we reach
	//this is dynamic
	long t0 = 0;
	//final int iterationTime = 10;
	
	//if time out
	boolean stopAlgorithm = false;
	
	//base number of dived the alpha legal region
	int AlphaTime = 10;
	//if record the tree
	final boolean ifRecord = false;
	//oldQ * this = ConvergeT
	final double RelativeErr = 0.01;

	//max time of iteratively shrink alpha
	final int MaxShrink = 5;
	//when we go beyond legal region, do we project back by
	//decreasing the same value for all vars or by times a factor
	final boolean ifProjectwtTime = false;
	
	final boolean ifPrint = false;
	final boolean ifPrintEst = false;
	final boolean ifPrintBit = false;
	boolean ifPrintInitial = false;
	final boolean ifPrintProb = false;
	//print out the starting and ending points of each random restart
	final boolean ifPrintGrid = false;
	final boolean ifDefaultNoop = true;
	
	//print trajectory actions step by step in the end
	final boolean ifPrintTraje = false;
	
	//print the routine updates
	final boolean ifPrintUpdateInRoutine = false;
	
	// if we already go over this depth, we use calculation rather than real estimate
	final int MaxEstimateDepth = 10;
	
	//if use forward accumulation or reverse accumulation
	final boolean ifReverseAccu = true;
	final double fixAlpha = -1;
	final boolean ifRecordRoutine = true;
	final boolean ifTopoSort = true;

	int maxDepth = 0;
	
	//stats
	double roundRandom = 0;
	double roundUpdates = 0;
	double roundSeen = 0;
	long sizeOfNonLeaf = 0;
	
	ArrayList<Double> bestNumAct = new ArrayList<Double>();
	double highestScore = -Double.MAX_VALUE;
	HashMap<ArrayList<Double>, Double> routine = new HashMap<ArrayList<Double>, Double>();
	HashMap<ArrayList<Double>, Double> initActRoutine = new HashMap<ArrayList<Double>, Double>();
	//ArrayList<ArrayList<Double>> triedAct = new ArrayList<ArrayList<Double>>();
	
	//a table transfers from actions to numbers
	//HashMap<Integer, HashMap<PVAR_NAME, HashMap<ArrayList<LCONST>, Integer>>> trans2Num = new HashMap<Integer, HashMap<PVAR_NAME,HashMap<ArrayList<LCONST>,Integer>>>();
	HashMap<Integer, HashMap<Integer, HashMap<PVAR_NAME, HashMap<ArrayList<LCONST>, TreeExp>>>> trans2Tree = new HashMap<>();
	ArrayList<PVAR_NAME> int2PVAR = new ArrayList<>();
	ArrayList<ArrayList<TYPE_NAME>> int2TYPE_NAME = new ArrayList<>();
	ArrayList<ArrayList<LCONST>> int2LCONST = new ArrayList<>();
	
	//THE ORIGINAL BELIEF RECORD
	HashMap<PVAR_NAME, HashMap<ArrayList<LCONST>, TreeExp>> stateOri = new HashMap<>();
	HashMap<PVAR_NAME, HashMap<ArrayList<LCONST>, TreeExp>> statePrimeOri = new HashMap<>();
	HashMap<PVAR_NAME, HashMap<ArrayList<LCONST>, TreeExp>> ObsOri = new HashMap<>();
	HashMap<PVAR_NAME, HashMap<ArrayList<LCONST>, Object>> intermOri = new HashMap<>();
	HashMap<PVAR_NAME, HashMap<ArrayList<LCONST>, TreeExp>> actionOri = new HashMap<>();
	
	//THE ORIGINAL BELIEF RECORD for initial belief
	HashMap<PVAR_NAME, HashMap<ArrayList<LCONST>, TreeExp>> stateOriIni = new HashMap<>();
	HashMap<PVAR_NAME, HashMap<ArrayList<LCONST>, TreeExp>> statePrimeOriIni = new HashMap<>();
	HashMap<PVAR_NAME, HashMap<ArrayList<LCONST>, TreeExp>> ObsOriIni = new HashMap<>();
	HashMap<PVAR_NAME, HashMap<ArrayList<LCONST>, Object>> intermOriIni = new HashMap<>();
	HashMap<PVAR_NAME, HashMap<ArrayList<LCONST>, TreeExp>> actionOriIni = new HashMap<>();
	
	double obsMinProb = 0.0001;
	
	ArrayList<PVAR_NAME> tmpObsNames = new ArrayList<>();
	HashMap<PVAR_NAME, HashMap<ArrayList<LCONST>, TreeExp>> tmpObs = new HashMap<>();
	HashMap<PVAR_NAME, RDDL.CPF_DEF> tmpCPF = new HashMap<>();
	HashMap<PVAR_NAME,PVARIABLE_DEF> tmpPVariables = new HashMap<>();
	
	long lastStepCounter = 0;
	
	long sumOfOneStepSize = 0;
	int oneStepSizeCounter = 0;
	
	
	//build the reward expectation function
	// with only the root level actions as variable
	// the other levels actions are treated as constant
	
	//record if there is any action variable not in the graph
	//would eventually set them to be false
	
	
	/**********************
	 * following code is for debuging use
	 * make sure all flags are set to be false before real run
	 * @param s
	 */
	//used to fix the move-left action to be true
	//domain: crossing_1
	public final boolean ifFixMoveLeft = false;
	
	
	public void OrderTypes(TEState s){
		Iterator thisIterator = s._hmTypes.entrySet().iterator(); 
		while (thisIterator.hasNext()) { 
		    Map.Entry entry = (Map.Entry) thisIterator.next();     
		    OBJECT_TYPE_DEF val = (OBJECT_TYPE_DEF)entry.getValue(); 
		    if(val._typeSuperclass != null){
		    	TYPE_NAME theSuper = val._typeSuperclass;
		    	if(!superClass.containsKey(val._sName)){
		    		superClass.put(val._sName, new ArrayList<>());
		    	}
		    	superClass.get(val._sName).add(theSuper);
		    	if(!childClass.containsKey(theSuper)){
		    		childClass.put(theSuper, new ArrayList<>());
		    	}
		    	childClass.get(theSuper).add(val._sName);
		    }
		}
	}
	
	//pass in a TEState with priori calculated
		//return a new TEState with s' revised and s' advanced
		public TreeExp ReviseState(TEState s, int[] trueObs, HashMap<PVAR_NAME, 
				HashMap<ArrayList<LCONST>, HashMap<PVAR_NAME, HashMap<ArrayList<LCONST>, Object>>>> condObsProb, boolean ifRestore) throws EvalException {
			
			//create a new copy of the original belief
			if(ifRestore) {
				RestoreStatePointer(s);
			}
			
			//newS.init(s);
			//based on the true observation variables, revise each s'
			
			//first figuer out the denorminator
			TreeExp denorm = TreeExp.BuildNewTreeExp(1.0, null);
			int countObsVar = 0; 
			int combIndex = 0;
			for(PVAR_NAME obsName: s._alObservNames) {
				for(ArrayList<LCONST> obsTerms: s.generateAtoms(obsName)) {
					if(combIndex < trueObs.length && trueObs[combIndex] == countObsVar) {
						combIndex ++;
						denorm = denorm.L_TIME(TEState.toTExp(s._observ.get(obsName).get(obsTerms), null));
					}
					else {
						denorm = denorm.L_TIME(TEState.toTExp(s._observ.get(obsName).get(obsTerms), null).L_NOT());
					}
					countObsVar ++;
				}
			}
			for(PVAR_NAME p: s._alStateNames) {
				for(ArrayList<LCONST> terms: s.generateAtoms(p)) {
					TreeExp revisedSPrime = TreeExp.BuildNewTreeExp(1.0, null);
					countObsVar = 0; 
					combIndex = 0;

					
					for(PVAR_NAME obsName: s._alObservNames) {
						for(ArrayList<LCONST> obsTerms: s.generateAtoms(obsName)) {
							if(combIndex < trueObs.length && trueObs[combIndex] == countObsVar) {
								combIndex ++;
								revisedSPrime = revisedSPrime.L_TIME(TEState.toTExp(condObsProb.get(obsName)
										.get(obsTerms).get(p).get(terms), null));
							}
							else {

								revisedSPrime = revisedSPrime.L_TIME(TEState.toTExp(condObsProb.get(obsName)
										.get(obsTerms).get(p).get(terms), null).L_NOT());
							}
							countObsVar ++;
						}
					}
					revisedSPrime = revisedSPrime.L_TIME(s._nextState.get(p).get(terms));

					revisedSPrime = revisedSPrime.L_DIV(denorm);

					s._nextState.get(p).remove(terms);
					s._nextState.get(p).put(terms, revisedSPrime);
				}
			}
			
			return denorm;
		}
		
		
	
	//pass in a TEState with priori calculated
	//return a new TEState with s' revised and s' advanced
	public TreeExp ReviseState(TEState s, HashMap<PVAR_NAME, 
			HashMap<ArrayList<LCONST>, HashMap<PVAR_NAME, HashMap<ArrayList<LCONST>, Object>>>> condObsProb, boolean ifRestore) throws EvalException {
		
		//create a new copy of the original belief
		if(ifRestore) {
			RestoreStatePointer(s);
		}
		
		//System.out.println("!!");
		//newS.init(s);
		//based on the true observation variables, revise each s'
		
		//first figuer out the denorminator
		TreeExp denorm = TreeExp.BuildNewTreeExp(1.0, null);

		//generate the grounding samplings
		ArrayList<TreeExp> randomNum = new ArrayList<>();
		ArrayList<TreeExp> groudObs = new ArrayList<>();
		for(PVAR_NAME p: s._alObservNames) {
  			for(ArrayList<LCONST> terms: s.generateAtoms(p)) {
				TreeExp randNum = TreeExp.BuildNewTreeExp(_random.nextUniform(0.0, 1.0), null);
				groudObs.add(TEState.toTExp(s._observ.get(p).get(terms), null).
						L_DEDUCT(randNum).L_TIME(TreeExp.BuildNewTreeExp(10, null)).SIG());
			}
		}
		
		int counter = 0;
		for(PVAR_NAME obsName: s._alObservNames) {
			for(ArrayList<LCONST> obsTerms: s.generateAtoms(obsName)) {
				TreeExp theObsMarg = TEState.toTExp(s._observ.get(obsName).get(obsTerms), null);
				denorm = denorm.L_TIME(theObsMarg.L_TIME(groudObs.get(counter))
						.L_ADD(theObsMarg.L_NOT().L_TIME(groudObs.get(counter).L_NOT())));
				counter ++;
				theObsMarg = null;
			}
		}
		
		for(PVAR_NAME p: s._alStateNames) {
			for(ArrayList<LCONST> terms: s.generateAtoms(p)) {
				TreeExp revisedSPrime = TreeExp.BuildNewTreeExp(1.0, null);
				counter = 0;
				for(PVAR_NAME obsName: s._alObservNames) {
					for(ArrayList<LCONST> obsTerms: s.generateAtoms(obsName)) {
						TreeExp theCondObsProb = TEState.toTExp(condObsProb.get(obsName)
								.get(obsTerms).get(p).get(terms), null);
						TreeExp theObs = groudObs.get(counter);
						revisedSPrime = revisedSPrime.L_TIME(theCondObsProb.L_TIME(theObs).
								L_ADD(theCondObsProb.L_NOT().L_TIME(theObs.L_NOT())));
						theObs = null;
						theCondObsProb = null;
						counter ++;
					}
				}
				
				revisedSPrime = revisedSPrime.L_TIME(s._nextState.get(p).get(terms));

				revisedSPrime = revisedSPrime.L_DIV(denorm);

				s._nextState.get(p).remove(terms);
				s._nextState.get(p).put(terms, revisedSPrime);
			}
		}
		
		return denorm;
	}
	
	class ExpandResults{
		HashMap<PVAR_NAME, HashMap<ArrayList<LCONST>, HashMap<PVAR_NAME, HashMap<ArrayList<LCONST>, Object>>>> condObsProb;
		TreeExp qGraph;
		public ExpandResults(HashMap<PVAR_NAME, HashMap<ArrayList<LCONST>, HashMap<PVAR_NAME, HashMap<ArrayList<LCONST>, Object>>>> c,
				TreeExp F) {
			condObsProb = c;
			qGraph = F;
		}
	}
	
	// a function that starts on any level
	// if endH is -1, pushes to the fixed (dynamic) depth
	// else push to endH
	public ExpandResults ExpandForAObs(TEState as, int startH, int endH, int obsIndex, double discount, TreeExp F,
			TreeSizeCounter treeCounter, double nextStepNodesNum, double updatePerNode, 
			ArrayList<Long> counterRecord, QuadraticEstimator qEst, boolean ifObserve, TreeExp obsProb)
			throws Exception {
		HashMap<PVAR_NAME, HashMap<ArrayList<LCONST>, HashMap<PVAR_NAME, HashMap<ArrayList<LCONST>, Object>>>> conObs = new HashMap<>();
		double cur_discount = Math.pow(discount, startH);
		int h = startH;
		if (endH == -1) {	
			endH = maxDepth;
		}
		


		trans2Tree.put(obsIndex, new HashMap<Integer, HashMap<PVAR_NAME, HashMap<ArrayList<LCONST>, TreeExp>>>());
		// get observation probability
		for (; h < endH; h++) {

			/*****************************
			 * create the action variable records
			 *******************************/
			// int2PVAR: integer to PVAR, arranged as initial actions, and then level 1
			// actions
			// (by observation), and then level 2, Level ...
			// int2TYPE_NAME: same as above but maps to the Arraylist<LCONST>
			// minimalProb: a long array that has minimal prob for eahc action bit, ordering
			// the same as above
			// act2int: only works for initial actions, maps a PVAR_NAME and
			// Arraylist<LCONST> to its number
			// trans2Tree: a hashmap taht maps h, observation, p and t to the actualy action
			// tree
			trans2Tree.get(obsIndex).put(h, new HashMap<PVAR_NAME, HashMap<ArrayList<LCONST>, TreeExp>>());
			for (PVAR_NAME p : as._alActionNames) {
				trans2Tree.get(obsIndex).get(h).put(p, new HashMap<ArrayList<LCONST>, TreeExp>());
				ArrayList<ArrayList<LCONST>> terms = as.generateAtoms(p);
				// find list of types of the p_var_name
				PVARIABLE_DEF pvar_def = as._hmPVariables.get(p);
				// list of type names of the pvar
				ArrayList<TYPE_NAME> theTypeNames = pvar_def._alParamTypes;
				for (ArrayList<LCONST> t : terms) {
					trans2Tree.get(obsIndex).get(h).get(p).put(t, new TreeExp((Integer) actionCounter, null));
					// trans2Num.get(h).get(p).put(t, actionCounter);

					// add the var name and type names to associate with the number

					int2PVAR.add(p);
					int2TYPE_NAME.add(theTypeNames);
					minimalProb.add(-Double.MAX_VALUE);

					if (h == 0) {
						if (!act2Int.containsKey(p)) {
							act2Int.put(p, new HashMap<>());
						}
						act2Int.get(p).put(t, actionCounter);
					}
					actionCounter++;

				}
			}

			// the groups of actions added by 1
			maxnumConformantG++;

			// Policy.stateHistory.add(as.QuickCopy());
			// if(_ifDisAction){
			// as.CalActionDiscount();
			// }
			// actions
			ArrayList<PVAR_INST_DEF> trajeActs = new ArrayList<RDDL.PVAR_INST_DEF>();

			for (PVAR_NAME p : as._alActionNames) {
				for (ArrayList<LCONST> t : as.generateAtoms(p)) {
					HashMap<PVAR_NAME, HashMap<ArrayList<LCONST>, TreeExp>> in1 = trans2Tree.get(obsIndex).get(h);
					HashMap<ArrayList<LCONST>, TreeExp> in2 = in1.get(p);
					TreeExp tr = in2.get(t);
					PVAR_INST_DEF theAct = new PVAR_INST_DEF(p._sPVarName, (Object) tr, t);
					trajeActs.add(theAct);
				}
			}

			as.SetActNoCompute(trajeActs);

			cur_discount *= discount;
			// output the current achieved depth
			if (ifPrint) {
				System.out.println("obs " + obsIndex + ": " + (h + 1) + " steps.");
			}

			// try three times of update
			if (searchDepth == -1) {
				long numOfNodes = treeCounter.nonLeafCounter;
				// System.out.println("try: " + numOfNodes);
				long timeLeft = _timeAllowed - (System.currentTimeMillis() - t0);
				// System.out.println("time left: " + timeLeft);
				//if (F.counter != 0) {
					 

					//System.out.println();
					// System.out.println("size: " + numOfNodes);
					// System.out.println("predict next step size: " + ((sumOfOneStepSize+1) / (oneStepSizeCounter+1)));
					// System.out.println("obs size: " + countObs);
					// System.out.println("obs index: " + obsIndex);
					// System.out.println("update per node update: " + updatePerNode);
					// System.out.println("Time left: " + timeLeft);
				//}//
			}

			// System.out.println("h: " + h);
			// if(h!=maxDepth - 1){
			if (!ifObserve) {
				as.computeNextState(_random);
			} else {
				// System.out.println("Enter obs cal");
				conObs = as.computeNextStateAndObs(_random);
				// System.out.println("conobs size: " + conObs.size());
			}
			TreeExp reward = TEState.toTExp(as._reward.sample(new HashMap<LVAR, LCONST>(), as, _random), null);
			// System.out.println(as);
			// System.out.println("reward: " + reward);
			F = F.L_ADD(obsProb.L_TIME(reward).L_TIME(TreeExp.BuildNewTreeExp(cur_discount, null)));
			treeCounter.Count(F);
			F.counter = treeCounter.nonLeafCounter;

			long thisStepNumNodes = F.counter - lastStepCounter;
			//System.out.println(thisStepNumNodes);
			if (ifPrintSizePredict) {
				System.out.println(thisStepNumNodes + " " + nextStepNodesNum);
			}
			sumOfOneStepSize += thisStepNumNodes;
			oneStepSizeCounter ++;

			// print out the real num of nodes and prediction (from last step) num of nodes
			
			// update prediction for next step
			//System.out.println(counterRecord);
			//nextStepNodesNum = qEst.PredictNextSize();
			//System.out.println(thisStepNumNodes + " " + nextStepNodesNum);
			// record this step
			lastStepCounter = F.counter;
			if (!ifObserve) {
				as.advanceNextState();
			}
			// System.out.println(h);
			// System.out.println(as);
			// }

			// check time
			
			// System.out.println(h);

		}
		avgConformantDepth += h;
		System.out.println("obs " + obsIndex + ": " + h + " steps!");

		ExpandResults res = new ExpandResults(conObs, F);
		return res;
	}
	
	public void UpdateBelief(State newPassIn) throws EvalException {
		//for first step of each round, use the pass in but set the belief to 0.5, 0.5
		if(ifInitialBelief) {
			belief = new TEState();
			State cs = new State();
			cs = (State)DeepCloneUtil.deepClone(newPassIn);
			belief.init(cs);
			belief._alObservNames = null;
			belief._alObservNames = tmpObsNames;
			belief._observ = null;
			belief._observ = tmpObs;
			//belief._hmCPFs = null;
			////belief._hmCPFs = tmpCPF;
			//belief._hmPVariables = null;
			//belief._hmPVariables = tmpPVariables;
			cs = null;
		}
		else {
			for(PVAR_NAME p: belief._alStateNames) {
				for(ArrayList<LCONST> terms: belief.generateAtoms(p)) {
					if(obs2State.containsKey(p) && newPassIn._observ.get(obs2State.get(p)) != null 
							&& newPassIn._observ.get(obs2State.get(p)).get(terms) != null) {
						System.out.println(p + " " + terms + " surely set to " + newPassIn._observ.get(obs2State.get(p)).get(terms));
						belief._state.get(p).put(terms, TEState.toTExp(newPassIn._observ.get(obs2State.get(p)).get(terms), null));
					}
				}
			}
			//if the observation is null
			for(PVAR_NAME p: newPassIn._alObservNames) {
				for(ArrayList<LCONST> terms: newPassIn.generateAtoms(p)) {
					if(newPassIn._observ.get(p).get(terms) == null) {
						belief.SetActNoCompute(lastAction);
						belief.computeNextState(_random);
						belief.advanceNextState();
						return;
					}
				}
			}
			//if the observation is not null
			belief.SetActNoCompute(lastAction);
			HashMap<PVAR_NAME, HashMap<ArrayList<LCONST>, 
			HashMap<PVAR_NAME, HashMap<ArrayList<LCONST>, Object>>>> obs = belief.computeNextStateAndObs(_random);
			int countObs = 0;
			ArrayList<Integer> combArray = new ArrayList<>();
			for(PVAR_NAME p: newPassIn._alObservNames) {
				for(ArrayList<LCONST> terms: newPassIn.generateAtoms(p)) {
					if((Boolean)newPassIn._observ.get(p).get(terms)) {
						combArray.add(countObs);
					}
					countObs ++;
				}
			}
			int[] comb = new int[combArray.size()];
			for(int i = 0; i < combArray.size(); i ++) {
				comb[i] = combArray.get(i);
			}
			ReviseState(belief, comb, obs, false);
			belief.advanceNextState();
		}
		
		//if(ifInitialBelief) {
			//System.out.println(newPassIn);
		//}
		
	}
	
	//record the state, obs and state prime
	public void RestoreStatePointer(TEState as) throws EvalException {
		for(PVAR_NAME p: as._alStateNames) {
			for(ArrayList<LCONST> terms: as.generateAtoms(p)) {
				as._state.get(p).put(terms, stateOri.get(p).get(terms));
				as._nextState.get(p).put(terms, statePrimeOri.get(p).get(terms));
			}
		}
		for(PVAR_NAME p: as._alObservNames) {
			for(ArrayList<LCONST> terms: as.generateAtoms(p)) {
				as._observ.get(p).put(terms, ObsOri.get(p).get(terms));
			}
		}
		for(PVAR_NAME p: as._alIntermNames) {
			for(ArrayList<LCONST> terms: as.generateAtoms(p)) {
				as._interm.get(p).put(terms, intermOri.get(p).get(terms));
			}
		}
		for(PVAR_NAME p: as._alActionNames) {
			for(ArrayList<LCONST> terms: as.generateAtoms(p)) {
				as._actions.get(p).put(terms, actionOri.get(p).get(terms));
			}
		}
	}
	
	public void RecordStatePointer(TEState as) throws EvalException {
		for(PVAR_NAME p: as._alStateNames) {
			for(ArrayList<LCONST> terms: as.generateAtoms(p)) {
				stateOri.get(p).put(terms, as._state.get(p).get(terms));
				statePrimeOri.get(p).put(terms, as._nextState.get(p).get(terms));
			}
		}
		for(PVAR_NAME p: as._alObservNames) {
			for(ArrayList<LCONST> terms: as.generateAtoms(p)) {
				ObsOri.get(p).put(terms, as._observ.get(p).get(terms));
			}
		}
		for(PVAR_NAME p: as._alIntermNames) {
			for(ArrayList<LCONST> terms: as.generateAtoms(p)) {
				intermOri.get(p).put(terms, as._interm.get(p).get(terms));
			}
		}
		for(PVAR_NAME p: as._alActionNames) {
			for(ArrayList<LCONST> terms: as.generateAtoms(p)) {
				actionOri.get(p).put(terms, as._actions.get(p).get(terms));
			}
		}
	}
	
	public void RecordInitBeliefPointer(TEState as) throws EvalException {
		for(PVAR_NAME p: as._alStateNames) {
			for(ArrayList<LCONST> terms: as.generateAtoms(p)) {
				stateOriIni.get(p).put(terms, as._state.get(p).get(terms));
				statePrimeOriIni.get(p).put(terms, as._nextState.get(p).get(terms));
			}
		}
		for(PVAR_NAME p: as._alObservNames) {
			for(ArrayList<LCONST> terms: as.generateAtoms(p)) {
				ObsOriIni.get(p).put(terms, as._observ.get(p).get(terms));
			}
		}
		for(PVAR_NAME p: as._alIntermNames) {
			for(ArrayList<LCONST> terms: as.generateAtoms(p)) {
				intermOriIni.get(p).put(terms, as._interm.get(p).get(terms));
			}
		}
		for(PVAR_NAME p: as._alActionNames) {
			if(as._actions.get(p) != null) {
				for(ArrayList<LCONST> terms: as.generateAtoms(p)) {
					if(as._actions.get(p).get(terms) != null) {
						actionOriIni.get(p).put(terms, as._actions.get(p).get(terms));
					}
					else {
						actionOriIni.get(p).put(terms, null);
					}
				}
			}
			else {
				actionOriIni.put(p, null);
			}
		}
	}
	
	public void RestoreInitBeliefPointer(TEState as) throws EvalException {
		for(PVAR_NAME p: as._alStateNames) {
			for(ArrayList<LCONST> terms: as.generateAtoms(p)) {
				as._state.get(p).put(terms, stateOriIni.get(p).get(terms));
				as._nextState.get(p).put(terms, statePrimeOriIni.get(p).get(terms));
			}
		}
		for(PVAR_NAME p: as._alObservNames) {
			for(ArrayList<LCONST> terms: as.generateAtoms(p)) {
				as._observ.get(p).put(terms, ObsOriIni.get(p).get(terms));
			}
		}
		for(PVAR_NAME p: as._alIntermNames) {
			for(ArrayList<LCONST> terms: as.generateAtoms(p)) {
				as._interm.get(p).put(terms, intermOriIni.get(p).get(terms));
			}
		}
		for(PVAR_NAME p: as._alActionNames) {
			if(as._actions.get(p) != null) {
				for(ArrayList<LCONST> terms: as.generateAtoms(p)) {
					if(as._actions.get(p).get(terms) != null) {
						as._actions.get(p).put(terms, actionOriIni.get(p).get(terms));
					}
					else {
						as._actions.get(p).put(terms, null);
					}
				}
			}
			else {
				as._actions.put(p, null);
			}
		}
	}
	
	TreeExp BuildF(State s) throws Exception {
		//clear the conformant depth record
		realConformantDepth.clear();
		maxnumConformantG = 0;
		actionCounter = 0;
		sumOfOneStepSize = 0;
		oneStepSizeCounter = 0;
		
		INSTANCE instance = _rddl._tmInstanceNodes.get(_sInstanceName);
		double cur_discount = 1;
		
		lastStepCounter = 0;
		// Q function
		TreeExp F = new TreeExp(0.0, null);
		
		//initialize and copy states for trajcotry
		//UpdateBelief(s);
		
		RecordInitBeliefPointer(belief);
		//TEState as = (TEState)DeepCloneUtil.deepClone(belief);
		//as.init(belief);
		
		//calculate concurrency
		if (ifConstructConstraints) {
			System.out.println("Rebuild constraints system!!!!");
			sumVars = new ArrayList<>();
			sumLimits = new ArrayList<>();
			sumLimitsExpr = new ArrayList<>();
			sumCoeffecients = new ArrayList<>();
			ifInSumConstraints = new Boolean[countActBits];
			Arrays.fill(ifInSumConstraints, false);
			try {
				HashMap<PVAR_NAME, HashMap<ArrayList<LCONST>, TreeExp>> tmpAction = new HashMap<>();
				int tmpCounter = 0;
				for(PVAR_NAME p: s._alActionNames) {
					tmpAction.put(p, new HashMap<>());
					for(ArrayList<LCONST> terms: s.generateAtoms(p)) {
						tmpAction.get(p).put(terms, new TreeExp(tmpCounter, null));
						tmpCounter ++;
					}
				}
				belief.AddExtraActionEff(tmpAction, sumVars, sumLimits, sumLimitsExpr, sumCoeffecients);
				tmpAction = null;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(0);
			}
			GetRandomTrajAct(belief);
			ifConstructConstraints = false;
		}
		
		//deal with hard constraints
		int countBase = 0;
		for (PVAR_NAME p : s._alActionNames) {
			ArrayList<ArrayList<LCONST>> ts = s.generateAtoms(p);
			if (Policy._extraEffects.containsKey(p)) {
				// addVars.addAll(c)
				ArrayList<TYPE_NAME> typenames = s._hmPVariables.get(p)._alParamTypes;
				HashMap<ArrayList<TYPE_NAME>, ArrayList<BOOL_EXPR>> possibleMaches = Policy._extraEffects.get(p);
				Iterator it = possibleMaches.entrySet().iterator();
				// first figure out which are the variables used in for this PVAR_NAME
				while (it.hasNext()) {
					Map.Entry pair = (Map.Entry) it.next();
					// first decide if the type of each parameter is a subclass of the type of
					// parameters in the preconditions
					ArrayList<TYPE_NAME> constraintsTypeName = (ArrayList<RDDL.TYPE_NAME>) pair.getKey();

					if (TEState.IfSuperClassList(typenames, constraintsTypeName)) {
						int countIndex = 0;
						for (ArrayList<LCONST> t : ts) {

							// times each additional effects to the action variables
							for (BOOL_EXPR theAddEff : (ArrayList<BOOL_EXPR>) pair.getValue()) {
								RandomDataGenerator newR = new RandomDataGenerator();
								// laod the substituion of lvars into newsub
								// pass new sub to the sampling of the constraints
								HashMap<LVAR, LCONST> newSubs = new HashMap<>();
								// deal with each parameter appeared in the precondition
								for (int i = 0; i < Policy._extraEffectsLVARS.get(p).get(constraintsTypeName)
										.size(); i++) {
									// important:
									// we assume that there is no repetition of types in both the preconditions and
									// action variable subs
									LVAR theVar = (LVAR) Policy._extraEffectsLVARS.get(p).get(constraintsTypeName)
											.get(i);
									newSubs.put(theVar, t.get(i));
								}
								if(newSubs.size() == 1 && theAddEff instanceof PVAR_EXPR && ((PVAR_EXPR)theAddEff)._alTerms.size() == 1) {
									Map.Entry<LVAR, LCONST> entry = newSubs.entrySet().iterator().next();
									LVAR key = entry.getKey();
									if(!key.toString().equals(((PVAR_EXPR)theAddEff)._alTerms.get(0).toString())) {
										LCONST value = entry.getValue();
										newSubs.put((LVAR)((PVAR_EXPR)theAddEff)._alTerms.get(0), value);
									}
									
								}
								TreeExp theT = TEState.toTExp(theAddEff.sample(newSubs, s, newR), null);
								if (theT.Is0()) {
									Policy.ifForcednotChoose[countBase + countIndex] = true;
									break;
								}
							}
							countIndex ++;
						}
					}
				}
				
			}
			countBase += ts.size();
		}
		
		//for(Boolean b: Policy.ifForcednotChoose){
		//	System.out.print(b + " ");
		//}
		//System.out.println();
		
		//build the super-child relations between all types in this instances
		//only do it once in each instances
		if(superClass.isEmpty()){
			System.out.println("Rebuild super classes!!!!");
			OrderTypes(belief);
		}
		
		//start calclulating trajectories
		int h = 0;
		routine.clear();
		double updatePerNode = 0.01;
		if(!ifFirstStep && numberNodesUpdates != 0) {
			updatePerNode = timeUsedForCal / numberNodesUpdates;
			System.out.println("Dealt with number of nodes times updates: " + numberNodesUpdates + " using time " + timeUsedForCal);
			System.out.println("Estimate calculation time per node per update: " + updatePerNode);
		}
		
		TreeSizeCounter treeCounter = new TreeSizeCounter();
		ArrayList<Long> counterRecord = new ArrayList<>();
		//estimation of number of nodes for next step
		double nextStepNodesNum = -1;
		QuadraticEstimator qEst = new QuadraticEstimator(counterRecord);
		ExpandResults res = ExpandForAObs(belief, 0, 1, 0, instance._dDiscount, F, 
				treeCounter, nextStepNodesNum, updatePerNode, counterRecord, qEst, true, TreeExp.BuildNewTreeExp(1.0, null));
		//System.out.println("finished 0 expansion");
		F = res.qGraph;
		RecordStatePointer(belief);
		

		skipCounter = 0;
		if(maxDepth != 1) {
			boolean ifSample = true;
			if(maxNumObs > countObs) {
				maxNumObs = (int)countObs;
				ifSample = false;
			}
			if(!ifSample) {
				int obsIndex = 0;
				if(countObs == 0) {
					belief.advanceNextState();
					F = ExpandForAObs(belief, 1, -1, obsIndex, instance._dDiscount, F, treeCounter, nextStepNodesNum, 
							updatePerNode, counterRecord, qEst, true, TreeExp.BuildNewTreeExp(1.0, null)).qGraph;
				}
				else {
					for(int r = 0; r <= countObsVars; r ++) {
						Iterator<int[]> iterator = CombinatoricsUtils.combinationsIterator(countObsVars, r);
						//System.out.println("combs generated");
					    while (iterator.hasNext()) {
					        final int[] combination = iterator.next();
					        //based on the observation, revise the belief
					       
					        TreeExp obsProb = ReviseState(belief, combination, res.condObsProb, true);

					        //System.out.println("state revised");
					        belief.advanceNextState();
					        //ready to push forward
					        if(!obsProb.Is0()) {
					        	
					        	F = ExpandForAObs(belief, 1, -1, obsIndex, instance._dDiscount, F, treeCounter, 
					        			nextStepNodesNum, updatePerNode, counterRecord, qEst, false, obsProb).qGraph;
					        	//System.out.println(F);
					        }
					        else {
					        	System.out.println("Skip obs: " + obsIndex);
					        	skipCounter ++;
					        }
					        obsIndex ++;
					        treeCounter.Count(F);
					        
					    }
					}
				}
			}
			else {
				for (int obsIndex = 0; obsIndex < maxNumObs; obsIndex++) {
					// based on the observation, revise the belief
					
					TreeExp obsProb = ReviseState(belief, res.condObsProb, true);
					// System.out.println("state revised");
					belief.advanceNextState();
					// ready to push forward

					F = ExpandForAObs(belief, 1, -1, obsIndex, instance._dDiscount, F, treeCounter, nextStepNodesNum,
								updatePerNode, counterRecord, qEst, false, TreeExp.BuildNewTreeExp(1.0 / maxNumObs, null)).qGraph;

					treeCounter.Count(F);
					
				}
			}

			if(currentRound < 5 && !ifFirstStep)
			_visCounter.depthInTotal += h;

			//double check that maxvardepth is at most same as h
			if(maxVarDepth > h){
				maxVarDepth = h;
			}
			
			if(ifPrint)
			System.out.println("We are finally using " + maxVarDepth + "-layer variables");
			
			//record the maxvardepth used temporarily
			//later in update step decide if add this to statistics
			//based on if having useful updates
			tmpVarDepthChange = maxVarDepth;
			tmpSearchDepthChange = maxDepth;
		}
		
		RestoreInitBeliefPointer(belief);
		
		res = null;
		//System.out.println(counterRecord);
		//System.out.println("buildf counter size: " + F.counter);
		return F;
	}
	
	//arbitarily search over legal region of alpha
	//use the best step length
	public double FndAlpha(State s, TreeExp F, ArrayList<Double> actionProb, ArrayList<Double> delta) throws EvalException{
		double maxAlpha = Double.MAX_VALUE;
		//we allow actionprob to go beyond 1
		//so first find the max prob and then acrrordingly find the space
		double maxProb = -1;
		for(double a: actionProb){
			if(a > maxProb){
				maxProb = a;
			}
		}
		maxProb += 1;
		//traverse each bit to shrink maxalpha
		for(int i = 0; i < actionProb.size(); i ++){
			double possibleAlpha = -1;
			if(delta.get(i) > 0){
				possibleAlpha = (maxProb-actionProb.get(i)) / Math.abs(delta.get(i));
			}
			if(delta.get(i) < 0){
				possibleAlpha = (actionProb.get(i) - (-1)) / Math.abs(delta.get(i));
			}
			if(possibleAlpha != -1 && possibleAlpha < maxAlpha){
				
				maxAlpha = possibleAlpha;
			}
		}
		//if we do concurrency projection then we need to again shrink the alpha by constraint the sum of prob be no bigger than
		//concurrency
		//System.out.println("max alpha is: " + maxAlpha);
		//now try alpha from 0 to maxAlpha
		double bestAlpha = 0;
		double bestQ = Double.NEGATIVE_INFINITY;
		ArrayList<Double> tempActProb = new ArrayList<Double>();
		for(int i = 0; i < actionProb.size(); i ++){
			tempActProb.add(0.0);
		}
		ArrayList<Double> bestActProb = new ArrayList<Double>();
		
		for(int i = 0; i < actionProb.size(); i ++){
			
			bestActProb.add(0.0);
		}
		
		// try to find the alpha with highest Q
		double realBest = -1;
		//double realNeeded = -1;
		
		// this is a loop to find smallest alpha because too large alpha could
		// be a problem
		// if we find in one iteration alpha is chosen to be the smallest among
		// possible then we extend another "alphatime"
		// between 0 and the smallest
		// maxAlpha = 0.2;
		int shrinkCounter = 0;
		double realNorm = 0;
		while (true) {
			if (fixAlpha == -1)
				bestAlpha = 0;
			else {
				bestAlpha = fixAlpha;
				AlphaTime = 1;
			}

			for (int i = 1; i <= AlphaTime; i++) {
				
				if (fixAlpha == -1){
					bestAlpha += maxAlpha / AlphaTime;
				}
				// System.out.println(bestAlpha);
				// update temp actprob
				
				for (int j = 0; j < actionProb.size(); j++) {
					double d = delta.get(j);
					double update = bestAlpha * d;
					double now = actionProb.get(j);
					
					if(now + update < 0){
						update = -now;
					}
					if(now + update > 1){
						update = 1 - now;
					}
					//norm += update * update;
					double newVal = now + update;

					tempActProb.set(j, newVal);
				}
				//norm = Math.sqrt(norm/actionProb.size());
				try {
					Projection(tempActProb);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				// update bestQ
				HashMap<TreeExp, Double> valRec = new HashMap<TreeExp, Double>();
				try {

					double theQ = F.RealValue(tempActProb, valRec);
					if (theQ > bestQ) {
						bestQ = theQ;
						// update actionProb
						for (int j = 0; j < actionProb.size(); j++) {
							bestActProb.set(j, tempActProb.get(j));
						}
						realBest = bestAlpha;
						double norm = 0;
						for(int j = 0; j < actionProb.size(); j ++){
							double diff = tempActProb.get(j) - actionProb.get(j);
							norm += diff * diff;
						}
						norm = Math.sqrt(norm / actionProb.size());
						realNorm = norm;
					}
				} catch (EvalException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// System.out.println("BestAlpha is :" + realBest);

			if (fixAlpha == -1 && realBest == maxAlpha / AlphaTime) {
				maxAlpha /= AlphaTime;
				shrinkCounter++;
				
				if (shrinkCounter > MaxShrink) {
					break;
				}
				// System.out.println("Alpha is too large, will try alpha between 0 and "
				// + maxAlpha );
			} else {
				break;
			}
		}
		
		if(convergeNorm != -1 && realNorm <= convergeNorm){
			ifConverge = true;
		}

		for (int j = 0; j < actionProb.size(); j++) {
			actionProb.set(j, bestActProb.get(j));
		}
		return bestQ;
	}
	
	public void Projection(ArrayList<Double> actionProb) throws Exception {
		
		// first masking actions that are not in the graph
		MaskingActions(actionProb);
		for (int h = 0; h < maxnumConformantG; h++) {
			//set minimal prob of each var in this step to -1
			int base = h * countActBits;
			for(int i = 0; i < countActBits; i ++) {
				minimalProb.set(base + i, -Double.MAX_VALUE);
			}
			//value record for each node
			//so long as this is in the same depth
			//the value record could be reused
			HashMap<LVAR, LCONST> valMap = new HashMap();
			//traverse each action var to see if any has forced condition
			//also find the highest marginal action variable in the exist force action
			int highestMarIndex = -1;
			double highestMar = -1.0;
			PVAR_NAME highestName = null;
			ArrayList<TYPE_NAME> highestTypeName = null;
			for(int i = 0; i < countActBits; i ++) {
				int j = i + base;
				PVAR_NAME theP = int2PVAR.get(j);
				if(_forceActionCondForall.containsKey(theP)) {
					ArrayList<TYPE_NAME> typenames = int2TYPE_NAME.get(j);
					HashMap<ArrayList<TYPE_NAME>, ArrayList<BOOL_EXPR>> possibleMaches = 
							Policy._forceActionCondForall.get(theP);
					Iterator it = possibleMaches.entrySet().iterator();
					//traverse and find each cond associated with it
				    while (it.hasNext()) {
				        Map.Entry pair = (Map.Entry)it.next();
				        //first decide if the type of each parameter is a subclass of the type of parameters in the preconditions
				        ArrayList<TYPE_NAME> constraintsTypeName = (ArrayList<RDDL.TYPE_NAME>)pair.getKey();
				        if(TEState.IfSuperClassList(typenames, constraintsTypeName)) {
				        	//traverse each cond
				        	for (BOOL_EXPR theforceCond : (ArrayList<BOOL_EXPR>)pair.getValue()) {
								RandomDataGenerator newR = new RandomDataGenerator();
								Object theCondVal = null;
								try {
									theCondVal = theforceCond.sample(valMap, stateHistory.get(h), newR);
									double theCondDouble = -1;
									if(theCondVal instanceof Double) {
										theCondDouble = (Double)theCondVal;
									}
									else {
										theCondDouble = ((TreeExp)theCondVal).RealValue(actionProb, new HashMap<>());
									}
									
									if(theCondDouble > 1 || theCondDouble < 0) {
										throw new Exception("value of forcing action condition can only be within 0 ~ 1!");
									}
									if(!(h == 0 && ifForcednotChoose[i]) && theCondDouble > minimalProb.get(j)) {
										minimalProb.set(j, theCondDouble);
									}
								} catch (EvalException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
				        }
				    }
				}
				//traverse each action variable to check exsits force action
				//find the one with highest marginals
				if(_forceActionCondExist.containsKey(theP)) {
					ArrayList<TYPE_NAME> typenames = int2TYPE_NAME.get(j);
					HashMap<ArrayList<TYPE_NAME>, ArrayList<Object>> possibleMaches = 
							Policy._forceActionCondExist.get(theP);
					Iterator it = possibleMaches.entrySet().iterator();
					//traverse and find each cond associated with it
				    while (it.hasNext()) {
				        Map.Entry pair = (Map.Entry)it.next();
				        //first decide if the type of each parameter is a subclass of the type of parameters in the preconditions
				        ArrayList<TYPE_NAME> constraintsTypeName = (ArrayList<RDDL.TYPE_NAME>)pair.getKey();

				        if(TEState.IfSuperClassList(typenames, constraintsTypeName)) {
				        	boolean ifIgnor = false;
				        	if(h == 0 && Policy.ifForcednotChoose[j]) {
				        		ifIgnor = true;
				        	}
				        	if( !ifIgnor && actionProb.get(j) > highestMar) {
				        		highestMar = actionProb.get(j);
				        		highestMarIndex = j;
				        		highestName = theP;
				        		highestTypeName = constraintsTypeName;
				        	}
				        }
				    }
				}
			}
			
			//deal with the exist force action
			//only deal with the highest marginal variable
			//traverse each cond
			if(highestMarIndex != -1) {
				for (Object theforceCond : (ArrayList<Object>)_forceActionCondExist.get(highestName).get(highestTypeName)) {
					RandomDataGenerator newR = new RandomDataGenerator();
					Object theCondVal = null;
					try {
						if(theforceCond instanceof BOOL_EXPR) {
							theCondVal = ((BOOL_EXPR)theforceCond).sample(valMap, stateHistory.get(h), newR);
						}
						if(theforceCond instanceof Double) {
							theCondVal = theforceCond;
						}
						double theCondDouble = -1;
						if(theCondVal instanceof Double) {
							theCondDouble = (Double)theCondVal;
						}
						else {
							theCondDouble = ((TreeExp)theCondVal).RealValue(actionProb, new HashMap<>());
						}
						
						if(theCondDouble > 1 || theCondDouble < 0) {
							throw new Exception("value of forcing action condition can only be within 0 ~ 1!");
						}
						if(theCondDouble > minimalProb.get(highestMarIndex)) {
							minimalProb.set(highestMarIndex, theCondDouble);
						}
					} catch (EvalException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
        	//System.out.println(minimalProb);
        	//set everything 
			for(int j = 0 ; j < countActBits; j ++) {
				if(minimalProb.get(base + j) > 0) {
					actionProb.set(base + j, minimalProb.get(base + j));
				}
				if(h == 0 && ifForcednotChoose[j]) {
					actionProb.set(j, 0.0);
				}
			}
			
			for (int c = 0; c < sumVars.size(); c++) {
				int concurrency = -1;
				if(sumLimitsExpr.get(c) == null) {
					concurrency = sumLimits.get(c);
				}
				else {
					TreeExp treeRes = (TreeExp)sumLimitsExpr.get(c).sample(new HashMap<>(), stateHistory.get(h), _random);
					if(treeRes.term != null && treeRes.term.var == -1) {
						concurrency = (int) Math.round(treeRes.ToNumber());
					}
					else {
						concurrency = (int)((TreeExp)treeRes).RealValue(actionProb, new HashMap<>());
					}
				}
				int morethan1Counter = 0;
				int numOfVar = sumVars.get(c).size();


				double sumOfProb = 0.0;


				double maxOfProb = Double.NEGATIVE_INFINITY;

				for (int k = 0; k < numOfVar; k++) {
					
					int j = sumVars.get(c).get(k) + h * countActBits;

					int currentDepth = h;
					double newVal = actionProb.get(j);
					if (newVal > 1) {
						morethan1Counter ++;
					}
					// update sum to do projection

					sumOfProb += newVal * sumCoeffecients.get(c).get(k);
					if (newVal > maxOfProb) {
						maxOfProb = newVal;
					}
				}

				// do projection for this step
				double sumP = sumOfProb;
				double adjustPar = Double.NaN;
				if (sumP > concurrency) {
					adjustPar = sumP - concurrency;
					double theRemain = adjustPar;
					int notZero = 0;
					for (int k = 0; k < numOfVar; k++) {
						int j = sumVars.get(c).get(k);
						int index = base + j;
						double theVal = actionProb.get(index);
						if (theVal > 0 && theVal != minimalProb.get(index)) {
							notZero += sumCoeffecients.get(c).get(k);
						}
					}
					while (theRemain > 0) {
						double del = theRemain / notZero;
						theRemain = 0;
						notZero = 0;
						for (int k = 0; k < numOfVar; k++) {
							int j = sumVars.get(c).get(k);
							int theCoe = sumCoeffecients.get(c).get(k);
							int index = base + j;
							double oldVal = actionProb.get(index);
							if (oldVal == 0 || oldVal == minimalProb.get(index)) {
								continue;
							}
							double newVal = oldVal - del;
							// if newVal < minimalProb
							// set back newVal to minimalProb
							// add back to remin
							if(minimalProb.get(index) > 0 && newVal < minimalProb.get(index)) {
								actionProb.set(index, minimalProb.get(index));
								theRemain += (minimalProb.get(index) - newVal) * theCoe;
							}
							else {
								if(newVal < 0) {
									actionProb.set(index, 0.0);
									theRemain += (0.0 - newVal) * theCoe;
								}
								else {
									actionProb.set(index, newVal);
								}
							}
							if (actionProb.get(index) > 0) {
								notZero += theCoe;
							}
						}
					}
				}
			}
			//deal with sums
			for(int i = 0; i < sumVars.size(); i ++) {
				if(ifEqual.get(i)) {
					double sumH = 0;
					int countInGraph = 0;
					for(int j = 0; j < sumVars.get(i).size(); j ++) {
						int thecoe = sumCoeffecients.get(i).get(j);
						int index = sumVars.get(i).get(j) + base;
						if(ifInSumConstraints[index - base] && !ifForcednotChoose[index - base]) {
							sumH += actionProb.get(index) * thecoe;
							countInGraph += thecoe;
						}
					}
					int concurrency = -1;
					if(sumLimitsExpr.get(i) == null) {
						concurrency = sumLimits.get(i);
					}
					else {
						concurrency = (int)sumLimitsExpr.get(i).sample(new HashMap<>(), stateHistory.get(h), _random);
					}
					double diff = concurrency - sumH;
					double addToBit = diff / countInGraph;
					for(int j = 0; j < sumVars.get(i).size(); j ++) {
						int index = sumVars.get(i).get(j) + base;
						if(ifInSumConstraints[index - base] && !ifForcednotChoose[index - base]) {
							double old = actionProb.get(index);
							actionProb.set(index, old + addToBit);
						}
					}
				}
			}
		}
	}
	
	//This must be called in the beginning of a projection
	public void MaskingActions(ArrayList<Double> actions) {
		for(int i = 0; i < actions.size(); i ++) {
			if(!ifInGraph[i]) {
				actions.set(i, 0.0);
			}
		}	
	}

	public ArrayList<Double> UpdateAllwtProj(State s, TreeExp F) throws EvalException{

		tmpUpdatesChange = 0;
		
		//initialize action variables in graph or not
		ifInGraph = new Boolean[countActBits * maxnumConformantG];
		for (int i = 0; i < countActBits * maxnumConformantG; i++) {
			ifInGraph[i] = false;
		}		
		
		ArrayList<Double> actionProb = new ArrayList<Double>();
		for(int i = 0; i < countActBits * maxnumConformantG; i ++){
			actionProb.add(0.0); 
		} 
		
		//ArrayList<TExp> visited = new ArrayList<TExp>();
		//int b = F.Size(visited );
		//iteration counter
		int randomRestart = 0;
		roundRandom = 0;
		roundUpdates = 0;
		roundSeen = 0;
		
		//Record the best actionProb that gets the highest Q value in F tree
		ArrayList<Double> bestActionProb = new ArrayList<Double>();
		for(int i = 0; i < countActBits; i ++){
			bestActionProb.add(0.0);
		}
		double bestQ = Double.NEGATIVE_INFINITY;
		ArrayList<Double> completeBest = new ArrayList<Double>();
		for(int i = 0; i < countActBits * maxnumConformantG; i ++){
			completeBest.add(0.0);
		}
		//generate concrete actions for getting the starting point of rrs

		//topological ordering, record two things
		//1. number of non-leaf nodes
		//2. action variables in the graph (ifIngraph[])
		ArrayList<TreeExp> que = new ArrayList<TreeExp>();
		if(ifReverseAccu){
			que = F.TopologQueue(ifTopoSort);
			if(currentRound < 5 && !ifFirstStep){
				_visCounter.sizeInTotal += F.numOfNonLeaf;

			}
			//System.out.println("Number of non-leaf nodes: " + F.numOfNonLeaf);
			//System.out.print("Action variables in graph: ");
			//for(int i = 0; i < ifInGraph.length - 1; i ++) {
			//	System.out.print(ifInGraph[i] + ", ");
			//}
			//System.out.println(ifInGraph[ifInGraph.length - 1]);
		}
		
		stopAlgorithm = false;
		double totalUpdateCounter = 0;
		//looping over rando restarts
		while(!stopAlgorithm){

			//flag for convergence
			ifConverge = false;
			
			//updates that being done in this random restart
			//note that these updates may not be all used in statistics
			double updatedasCounter = 0;

			//initialize the action bits to 0 and 1 randomly
			for (int i = 0; i < actionProb.size(); i++) {
				actionProb.set(i, _random.nextUniform(0, 1.0));
			}
			
			try {
				Projection(actionProb);
			} catch (Exception e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}


			//evaluate the initial action bits
			try {
				Projection(actionProb);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			HashMap<TreeExp, Double> valRec = new HashMap<TreeExp, Double>();
			HashMap<TreeExp, Double> gradRec = new HashMap<TreeExp, Double>();
			//initialize oldQ to be realvalue calculated with initial action prob
			double oldQ = F.RealValue(actionProb, valRec);
			
			if(ifRecordRoutine){
				UpdateRoutine(F, s, actionProb, true);
			}
			
			//this is used to judge whether Q has been changed
			double initialQ = oldQ;
			if(ifPrintInitial){
				System.out.println(actionProb + " " + initialQ);
			}
			if(ifPrint){
				System.out.println("Q is initlaized to: " + oldQ);
			}
			
			//update bestQ and action
			if(oldQ > bestQ){
				bestQ = oldQ;
				for(int a = 0; a < countActBits; a ++){
					bestActionProb.set(a, actionProb.get(a));
				}
				for(int a = 0; a < actionProb.size(); a ++){
					completeBest.set(a, actionProb.get(a));
				}
			}
			
			//dead bit record
			//if during this random restart, certain bits never change, it means that Q is not related to it
			//set it to be 0
			//only for top level 
			ArrayList<Boolean> ifthisBitChange = new ArrayList<Boolean>();
			for(int a = 0; a < actionProb.size(); a ++){
				ifthisBitChange.add(false);
			}
			if(ifPrintBit){
				System.out.println();
				for(int a = 0; a < actionProb.size(); a++){
					System.out.println("a for " + "v" + a + " " + actionProb.get(a));
				}
				System.out.println();
			}
			//initialize newQ
			double newQ = 0; // this will be recalculated later
			
			//one random restart
			//quit when either ifconverge = true (this is udpated in fndalpha)
			//or running out of time
			while(!ifConverge && !stopAlgorithm){

				//calculate delta
				ArrayList<Double> delta = new ArrayList<Double>();
				try {
					F.RevAccGradient(ifTopoSort, que, delta, actionProb);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(ifFixMoveLeft) {
					for(int i = 0; i < 9; i ++) {
						delta.set(i, 0.0);
					}
				}
				
				//do updates for each bit
				if (ifPrintBit) {
					for (int i = 0; i < actionProb.size(); i++) {
						System.out.println("d for " + "v" + i + " " + delta.get(i));
					}
				}
				//record if a bit is changed
				for (int i = 0; i < delta.size(); i++) {
					double d = delta.get(i);
					if (d != 0) {
						ifthisBitChange.set(i, true);
					}
				}

				updatedasCounter ++;
				totalUpdateCounter ++;
				//this step updates prob and return the Q
				newQ = FndAlpha(s, F, actionProb, delta);
				//System.out.println(routine.size());
				if(ifRecordRoutine){
					UpdateRoutine(F, s, actionProb, true);
				}

				if(ifPrintBit){
					for(int a = 0; a < actionProb.size(); a++){
						System.out.println("a for " + "v" + a + " " + actionProb.get(a));
					}
				}
				
				//now alphas are changed so we need to clear the value record in the tree
				valRec.clear();

				if(ifPrint){
					System.out.println("oldQ: " + oldQ + "\n");
					System.out.println("newQ: " + newQ + "\n");
				}

				oldQ = newQ;
				//we don't need to clear valrec again
				//because the value when calculating newQ can be reused in next iteration
				//System.out.println(totalUpdateCounter);
				if(totalUpdateCounter > 200){
					stopAlgorithm = true;
					break;
				}
			}
			
			//converged; continue to next random restart
			//record statics only if the Q value has been changed during the updates
			if(newQ != initialQ){
				roundRandom ++;
				roundUpdates += updatedasCounter;
				updatesIntotal += updatedasCounter;
				if(currentRound < 5 && !ifFirstStep){
					_visCounter.updatesInTotal += updatedasCounter;
					_visCounter.randomInTotal ++;
				}
				tmpUpdatesChange += updatedasCounter;
			}
			if(ifPrint){
				if(ifConverge){
					System.out.println("RR: " + randomRestart + "converged!");
				}
				else{
					System.out.println("RR: " + randomRestart + "forced to stop because running out of time.");
				}
			}
			//Get the Q value for this convergence
			if(newQ > bestQ){
				if(ifPrint){
					System.out.println("Previous best is: " + bestQ + " and now the Q is: " + newQ);
				}
				
				bestQ = newQ;
				for(int a = 0; a < countActBits; a ++){
					bestActionProb.set(a, actionProb.get(a));
				}
				for(int a = 0; a < actionProb.size(); a ++){
					completeBest.set(a, actionProb.get(a));
				}
				
				//if an action bit is not changed
				//set it to be false
				if(ifDefaultNoop){
					for (int a = 0; a < countActBits; a++) {
						if (!ifthisBitChange.get(a)) {
							bestActionProb.set(a, 0.0);
						}
					}
				}
			}
			else{
				if(ifPrint){
					System.out.println("Failed to update Q. Previous best is: " + bestQ + " and now the Q is: " + newQ); 
				}		
			}
			
			if(ifDefaultNoop){
				//System.out.println(ifthisBitChange);
				for (int a = 0; a < countActBits; a++) {
					//System.out.println(ifthisBitChange);
					if (!ifthisBitChange.get(a)) {
						bestActionProb.set(a, 0.0);
					}
				}
			}
		}
		/*
		if(roundRandom == 0){
			_visCounter.updateTime --;
			_visCounter.SeenTime --;
		}
		*/
		if(ifPrintGrid){
			System.out.println("In total: " + randomRestart);
		}
		//record how many random restart have been done
		String countingStr = new String();
		countingStr += "\n\n*************************\n"
				     + "\n******** Summary ********\n"
				     + "\n*************************\n";
		countingStr += "Number of Random Restart: " + roundRandom + "\n";
		countingStr += "Number of Updates: " + roundUpdates + "\n";
		countingStr += "Number of Actions Seen: " + roundSeen + "\n";
		System.out.println(countingStr);
		
		//printout the action probs
		if(ifPrintBit){
			System.out.println("\nfinal action prob: ");
			for(double a: bestActionProb){
				System.out.println(a);
			}
		}
		
		System.out.println("best: " + bestQ);
		
		if(tmpUpdatesChange != 0){
			avgUpdates += tmpUpdatesChange;
			avgSearchDepth += tmpSearchDepthChange;
			//System.out.println("add " + tmpUpdatesChange + ", get " + avgUpdates);
			avgVarDepth += tmpVarDepthChange;
			effectiveSteps ++;
			//System.out.println(tmpVarDepthChange + " " + tmpSearchDepthChange);
			//System.out.println("effective: " + effectiveSteps);
		}
		

		//System.out.println(completeBest);
		//reset the max var depth
		//according to the current ratio setup
		//maxVarDepth = new Double(searchDepth * theRatio).intValue();
		System.out.println(initActRoutine);
		return bestActionProb; 
	}
	
	public ArrayList<Double> SampleNumAct(ArrayList<Double> varVal, State s) throws EvalException{
		int size = varVal.size();

		
		//find best action level wise
		//if use conformant, then do this for all trajectory level
		//otherwise only do this for first step
		int conformantDepth = ifConformant ? maxnumConformantG : 1;

		boolean[] mute = new boolean[conformantDepth * countActBits];
		double[] res = new double[conformantDepth * countActBits]; 
		for(int h = 0; h < conformantDepth; h ++){
			ArrayList<Integer> sumForEachCons = new ArrayList<>();
			for(int c = 0; c < sumVars.size(); c ++) {
				sumForEachCons.add(0);
			}
			for(int c = 0; c < sumVars.size(); c ++) {
				int concurrency = -1;
				if(sumLimitsExpr.get(c) == null) {
					concurrency = sumLimits.get(c);
				}
				else {
					TreeExp treeRes = (TreeExp)sumLimitsExpr.get(c).sample(new HashMap<>(), stateHistory.get(h), _random);
					if(treeRes.term != null && treeRes.term.var == -1) {
						concurrency = (int) Math.round(treeRes.ToNumber());
					}
					else {
						System.out.println("Sampling result can only be a number!");
						System.exit(0);
					}
				}
				int numVar = sumVars.get(c).size();
				for(int i = 0; i < concurrency; i ++){
					double max = -Double.MAX_VALUE;
					int maxIndex = -1;
					for(int k = 0; k < numVar; k ++){
						int j = h * countActBits + sumVars.get(c).get(k);
						if(!mute[j] && varVal.get(j) > max){
							max = varVal.get(j);
							maxIndex = j;
						}
					}
					
					if(max > randomAction.get(c) || ifEqual.get(c)){
						//the new added act bit might break some concurrency
						// need to check each of them by adding a sum to it
						int initialBit = maxIndex - h * countActBits;
						Boolean ifBreak = false;
						ArrayList<Integer> backupSum = new ArrayList<>();
						for(int c2 = 0; c2 < sumVars.size(); c2 ++) {
							int concurrency2 = -1;
							if(sumLimitsExpr.get(c2) == null) {
								concurrency2 = sumLimits.get(c2);
							}
							else {
								Object objectRes = sumLimitsExpr.get(c2).sample(new HashMap<LVAR, LCONST>(), s, _random); 
								if(objectRes instanceof Double) {
									concurrency2 = (int)Math.round((Double)objectRes);
								}
								else {
									TreeExp treeRes = (TreeExp)objectRes;
									if(treeRes.term == null || treeRes.term.var != -1) {
										System.out.println("Sampling result can only be a number!");
										System.exit(0);
									}
									else {
										concurrency2 = (int)treeRes.ToNumber();
									}
								}
							}
							if(sumVars.get(c2).contains(initialBit)) {
								int theIndex = sumVars.get(c2).indexOf(initialBit);
								int theSum = sumForEachCons.get(c2) + sumCoeffecients.get(c2).get(theIndex);
								if(theSum > concurrency2) {
									ifBreak = true;
									break;
								}
								else {
									backupSum.add(theSum);
								}
							}
							else {
								backupSum.add(sumForEachCons.get(c2));
							}
						}
						if(ifBreak) {
							break;
						}
						else {
							sumForEachCons = backupSum;
							res[maxIndex] = 1;
							mute[maxIndex] = true;
						}
					}
					else{
						break;
					}
				}
				
			}
		}
		
		for(int i = 0; i < countActBits; i ++) {
			int index = i;
			if(!ifInSumConstraints[i] && varVal.get(index) > 0.5) {
				res[index] = 1;
			}
		}

		ArrayList<Double> numAct = new ArrayList<Double>();
		for(double r: res){
			numAct.add(r);
		}
		//if we are not doing conformant
		//need to add fractional actions in the future steps
		if(!ifConformant) {
			for(int j = countActBits; j < size; j ++){
				numAct.add(varVal.get(j));
			}
		}

		return numAct;
	}
	
	public void UpdateRoutine(TreeExp F, State s, ArrayList<Double> varVal, boolean ifStatistics) throws EvalException{
		//based on varVal, sample concrete action
		ArrayList<Double> closestAct = SampleNumAct(varVal, s);
		ArrayList<Double> realAct = new ArrayList<Double>();
		for(int i = 0; i < countActBits; i ++){
			realAct.add(closestAct.get(i));
		}

		//evaluate the whole action including trajectories together
		if(!routine.containsKey(closestAct)){
			double val = 0;
			HashMap<TreeExp, Double> valRec = new HashMap<TreeExp, Double>();
			val = F.RealValue(closestAct, valRec);
			routine.put(closestAct, val);
			//update highest score and the best init action
			if(val > highestScore  || bestNumAct.size() == 0){
				//System.out.println(varVal);
				//System.out.println("new best: " + val);
				highestScore = val;
				bestNumAct = closestAct;
				
				//could try to output every new update action including the trajectory
				if(ifPrintUpdateInRoutine) {
					PrintUpdateInRoutine(val, s, closestAct);
				}
			}
			//update the record for init action evaluation
			if(!initActRoutine.containsKey(realAct)) {
				initActRoutine.put(realAct, val);
				if(ifStatistics){
					roundSeen ++;
					if(currentRound < 5  && !ifFirstStep){
						_visCounter.SeenInTotal ++;
					}
				}
			}
			else {
				if(val > initActRoutine.get(realAct)) {
					initActRoutine.put(realAct, val);
				}
			}
		}
	}
	
	//sample action from largest to smallest; build actions incrementally
	public void SampleAction(State s, ArrayList<ArrayList<PVAR_INST_DEF>> conformantActs, ArrayList<Double> useNum) throws EvalException{

		//find the best concrete action directly
		for(int h = 0; h < maxnumConformantG; h ++) {
			ArrayList<PVAR_INST_DEF> finalAction = new ArrayList<RDDL.PVAR_INST_DEF>();
			int counter = 0;
			for(PVAR_NAME p: s._alActionNames){
				for(ArrayList<LCONST> t: s.generateAtoms(p)){
					double resptNum = useNum.get(counter + h * countActBits);
					boolean ifChosen = false;
					Object actionVal = null;
					if(h == 0) {
						if(resptNum == 1.0) {
							ifChosen = true;
						}
						actionVal = true;
					}
					else {
						if(ifConformant) {
							if(resptNum == 1.0) {
								ifChosen = true;
							}
							actionVal = true;
						}
						else {
							if(resptNum > 0.0) {
								ifChosen = true;
							}
							actionVal = resptNum;
						}
					}
					if(ifChosen){
						finalAction.add(new PVAR_INST_DEF(p._sPVarName, actionVal, t));
					}
					counter ++;
				}
			}
			conformantActs.add(finalAction);
		}
	}
	

	public void GetRandomTrajAct(TEState s) throws Exception{
		//the real number of each action bit for taking random policy
		for(int c = 0; c < sumVars.size(); c ++) {
			int numVarBit = sumVars.get(c).size();
			int numVar = 0;
			for(int j = 0; j < numVarBit; j ++) {
				numVar += Policy.sumCoeffecients.get(c).get(j);
			}
			
			ArrayList<Double> comb = new ArrayList<Double>();
			Double numInTotal = 0.0;

			// caculate the number of choose k from n
			int theSumLimit = -1;
			if(sumLimitsExpr.get(c) == null) {
				theSumLimit = sumLimits.get(c);
			}
			else {
				TreeExp treeRes = (TreeExp)sumLimitsExpr.get(c).sample(new HashMap<LVAR, LCONST>(), s, _random); 
				if(treeRes.term == null || treeRes.term.var != -1) {
					System.out.println("Sampling result can only be a number!");
					System.exit(0);
				}
				else {
					theSumLimit = (int)treeRes.ToNumber();
				}
			}
			for (int k = 0; k <= theSumLimit; k++) {
				int max = numVar;
				double resu = 1;
				for (int j = 1; j <= k; j++) {
					resu *= max;
					max--;
				}
				for (int j = 2; j <= k; j++) {

					resu /= j;
				}
				// now res the is number of combs (choose j from countActBits)
				comb.add(resu);
				numInTotal += resu;
			}

			// now cal the marginal for random policy
			double marRandom = 0;
			for (int k = 1; k <= theSumLimit; k++) {
				marRandom += Double.valueOf(k) / numVar * comb.get(k)
						/ numInTotal;
			}

			randomAction.add(marRandom);
		}
		
	}
	
	//final 
	public void GenObsStateMap(State s) throws EvalException {
		//HashMap<PVAR_NAME, HashMap<ArrayList<LCONST>, Object>> tmpObs4State = new HashMap<>();
		for(PVAR_NAME p: s._alObservNames) {
			if(s._hmCPFs.get(p)._exprEquals instanceof RDDL.KronDelta) {
				RDDL.EXPR theInside = ((RDDL.KronDelta)s._hmCPFs.get(p)._exprEquals)._exprIntValue;
				if(theInside instanceof PVAR_EXPR) {
					PVAR_EXPR theInsideP = (PVAR_EXPR)theInside;
					if(s._nextState.containsKey(theInsideP._pName._pvarUnprimed)) {
						obs2State.put(theInsideP._pName._pvarUnprimed, p);
						continue;
					}
				}
			}
			tmpObsNames.add(p);
			tmpObs.put(p, new HashMap<>());
			for(ArrayList<LCONST> terms: s.generateAtoms(p)) {
				if(s._observ.get(p).get(terms) == null){
					tmpObs.get(p).put(terms, null);
				}
				else {
					tmpObs.get(p).put(terms, TEState.toTExp(s._observ.get(p).get(terms), null));
				}
			}
			//tmpObs4State.put(p, s._observ.get(p));
			
			//tmpCPF.put(p, s._hmCPFs.get(p));
			//tmpPVariables.put(p, s._hmPVariables.get(p));
		}
		//s._observ = null;
		//s._observ = tmpObs4State;
		//s._alObservNames = null;
		//s._alObservNames = tmpObsNames;
	}
	
	//final get action algorithm
	@Override
	public ArrayList<PVAR_INST_DEF> getActions(State s) throws EvalException {

		//check which observations are true observations
		if(ifDetectTrueObs) {
			GenObsStateMap(s);
			ifDetectTrueObs = false;
		}
		
		UpdateBelief(s);
		
		System.out.println("from client################");
		System.out.println(belief._state.get(new PVAR_NAME("agent-at")));
		System.out.println(belief._state.get(new PVAR_NAME("ghost-at")));

		//stats
		roundRandom = 0;
		roundUpdates = 0;
		roundSeen = 0;
		updatesIntotal = 0;
		//update action probs
		highestScore = -Double.MAX_VALUE;
		routine = new HashMap<ArrayList<Double>, Double>();
		initActRoutine = new HashMap<>();
		//count number of observatiosn
		countObsVars = 0;
		for(PVAR_NAME p: belief._alObservNames) {
			countObsVars += belief.generateAtoms(p).size();
		}
		if(countObsVars == 0) {
			countObs = 0;
		}
		else {
			countObs = Math.pow(2.0, countObsVars);
		}
		
		System.out.println(countObs + " observations in total");
		//clear the action records
		int2PVAR.clear();
		int2TYPE_NAME.clear();
		minimalProb.clear();
		//every time get to this point, meaning we have one more time of record of how many random restart have been tried
		if(currentRound < 5 && !ifFirstStep){
			_visCounter.randomTime ++;
			_visCounter.updateTime ++;
			_visCounter.SeenTime ++;
			_visCounter.depthTime ++;
			_visCounter.sizeTime ++;
		}
		t0 = System.currentTimeMillis();
		
		//initialize the belief record if needed
		if(stateOri.size() == 0) {
			for(PVAR_NAME p: s._alStateNames) {
				stateOri.put(p, new HashMap<>());
				statePrimeOri.put(p, new HashMap<>());
			}
			for(PVAR_NAME p: s._alObservNames) {
				ObsOri.put(p, new HashMap<>());
			}
			for(PVAR_NAME p: s._alIntermNames) {
				intermOri.put(p, new HashMap<>());
			}
			for(PVAR_NAME p: s._alActionNames) {
				actionOri.put(p, new HashMap<>());
			}
		}
		
		//initial lize the initla belief record
		if(stateOriIni.size() == 0) {
			for(PVAR_NAME p: s._alStateNames) {
				stateOriIni.put(p, new HashMap<>());
				statePrimeOriIni.put(p, new HashMap<>());
			}
			for(PVAR_NAME p: s._alObservNames) {
				ObsOriIni.put(p, new HashMap<>());
			}
			for(PVAR_NAME p: s._alIntermNames) {
				intermOriIni.put(p, new HashMap<>());
			}
			for(PVAR_NAME p: s._alActionNames) {
				actionOriIni.put(p, new HashMap<>());
			}
		}
		
		// declare a action list
		ArrayList<PVAR_INST_DEF> actions = new ArrayList<RDDL.PVAR_INST_DEF>();
		
		//recalculate the rootinit
		//because we assume there is no constraint so simple use concurrency divided by number of action bits
		if(countActBits == 0){
			for(PVAR_NAME p: belief._alActionNames){
				countActBits += belief.generateAtoms(p).size();
			}
		}
		
		
		
		//adjust maxVarDepth so that maxVarDepth * countActBits > baseVarNum
		// final F function
		INSTANCE instance = _rddl._tmInstanceNodes.get(_sInstanceName);
		if(searchDepth == -1){
			maxDepth = (instance._nHorizon - currentRound)  > instance._nHorizon ? instance._nHorizon : (instance._nHorizon - currentRound);
		}
		else{
			maxDepth = (instance._nHorizon - currentRound)  > searchDepth ? searchDepth : (instance._nHorizon - currentRound);
		}
		//counter = 0;
		stopAlgorithm = false;
		
		//initialize action prob
		ArrayList<Double> actionProb = null;

		//because maxVarDpeth could be too small when
		if (searchDepth == -1) {
			maxVarDepth = expectMaxVarDepth;
		}
		else {
			maxVarDepth = searchDepth < expectMaxVarDepth ? searchDepth : expectMaxVarDepth;
		}
		
		maxnumConformantG = 0;
		
		
		
		//decide using dynamic depth
		//searchDepth = -1;
		//decide using conformant
		if(theRatio == -1 && maxVarDepth >= 1) {
			//use fixed conformant depth
			System.out.println("Use conformant depth: " + maxVarDepth);
		}
		else {
			//use searchdepth * theRatio as conformant depth
			if(theRatio != -1 && maxVarDepth == -1) {
				System.out.println("Use conformant depth as ratio: " + theRatio);
			}
			else {
				try {
					throw new Exception("theRatio or maxVarDepth set incorrectly. Please check conformant set up!");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		//clear the state history
		//ready for building new history record
		stateHistory = new ArrayList<>();
		
		//forced not choose should be used only for intiial actions
		//if it is not the case should consider using array size countActBits * countConformantG
		ifForcednotChoose = new Boolean[countActBits];

		for(int i = 0; i < countActBits; i ++) {
			ifForcednotChoose[i] = false;
		}
		
		TreeExp F = null;
		try {
			F = BuildF(s);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Average confomant depth: " + (avgConformantDepth / (countObs - skipCounter)));
		avgConformantDepth = 0;
		Long t1 = System.currentTimeMillis();

		
		//total number of updates used in this step

		actionProb = UpdateAllwtProj(s, F);
		
		//calculate the time and counts for this step
		double thisUpdateTime = System.currentTimeMillis() - t1;
		//System.out.println(thisUpdateTime + " " + F.counter);
		double thisNumberNodesUpdates = F.counter * updatesIntotal;
		double thisTimePerNodeUpdate = thisUpdateTime / thisNumberNodesUpdates;
		//System.out.println("Average update time estimation based on single round: " + thisTimePerNodeUpdate);
		//System.out.println("This step bumber of nodes: " + (long)Math.ceil(thisNumberNodesUpdates));
		//System.out.println("avg dradient cost: " + gradientCost / thisNumberNodesUpdates);
		//System.out.println("avg fndalpa cost: " + fndalhpaCost / thisNumberNodesUpdates);
		timeHis.addLast(thisUpdateTime);
		nodesupdateHis.addLast(thisNumberNodesUpdates);
		if(thisNumberNodesUpdates > 0 && timeHis.size() > 3) {
			timeHis.removeFirst();
			nodesupdateHis.removeFirst();
		}
		timeUsedForCal = 0;
		numberNodesUpdates = 0;
		//for(int j = 0; j < timeHis.size(); j ++) {
		for(int j = timeHis.size() - 1; j >= 0; j --) {
			if(nodesupdateHis.get(j) > 0) {
				timeUsedForCal = timeHis.getLast();
				numberNodesUpdates = nodesupdateHis.getLast();
				break;
			}
		}

		
		//state history is not useful any more
		//free the memory
		stateHistory.clear();
		stateHistory = null;
		
		//fnd conformant actions for each level
		ArrayList<ArrayList<PVAR_INST_DEF>> conformantActs = new ArrayList<>();
		SampleAction(s, conformantActs, bestNumAct);
		bestNumAct.clear();
		bestNumAct = null;
		bestNumAct = new ArrayList<>();
		//ending work
		TreeExp.ClearHash();
		RDDL.ClearHash();
		
		if(ifPrintTraje) {
			System.out.println("********* trajectory actions **********");
			PrintTraje(conformantActs);
		}
		lastAction = conformantActs.get(0);
		return conformantActs.get(0);
	}
	
	/*************************
	 * printing functions
	 *************************/
	public void PrintTraje(ArrayList<ArrayList<PVAR_INST_DEF>> conformantActs) {
		for(int h = 0; h < conformantActs.size(); h ++) {
			System.out.println("h = " + h + ": " + conformantActs.get(h));
		}
	}

	//call if want to print the trajectory actions from the new num actions
	public void PrintUpdateInRoutine(double val, State s, ArrayList<Double> closetAct) {
		
		System.out.println("\nUpdated the val to: " + val);
		ArrayList<ArrayList<PVAR_INST_DEF>> conformantActs = new ArrayList<>();
		try {
			SampleAction(s, conformantActs, closetAct);
		} catch (EvalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PrintTraje(conformantActs);
	}
}
