-------------------------------------------------------------------------
OVERVIEW

SNAP is a state-of-the-art stochastic online planning algorithm for POMDPs. It uses the idea of "Aggregate Simulation" to build a computation graph which symbolically approximates the Q value of actions given the current state, and searches the action space very efficiently using gradient updates. It supports various action constraints defined in first-order predicates. It directly works with MDP domains defined in RDDL, and is implemented based on the original code of the RDDL simulator (https://github.com/ssanner/rddlsim). This source code corresponds to the NeurIPS paper Sampling Networks and Aggregate Simulation for Online POMDP Planning, PDF coming soon.
   
Please cite our paper if you are using the planner as a baseline, and/or star the project if you like it ^_^

-------------------------------------------------------------------------
RELATED PAPERS

* H. Cui, T. Keller and R. Khardon, Stochastic Planning with Lifted Symbolic Trajectory Optimization, Proceedings of the International 
Conference on Automated Planning and Scheduling (ICAPS), 2019.

* H. Cui and R. Khardon, Lifted Stochastic Planning, Belief Propagation and Marginal MAP, The AAAI-18 Workshop on Planning and Inference,
held with the AAAI Conference on Artificial Intelligence (AAAI), 2018

* H. Cui and R. Khardon, Online Symbolic Gradient-Based Optimization for Factored Action MDPs, International Joint Conference on 
Artificial Intelligence (IJCAI), 2016 

* H. Cui, R. Khardon, A. Fern, and P. Tadepalli., Factored MCTS for Large Scale Stochastic Planning. , Proceedings of the AAAI Conference
on Artificial Intelligence (AAAI), 2015

You can easily find any paper of your interest at Hao Cui's personal page
   https://sites.google.com/view/hao-cui/home
   
-------------------------------------------------------------------------
HOW TO USE

* Prerequisites: Java SE 1.8 or higher
* Compile
  In the SOGBOFA/ directory, type command ./compile
* Run the server
  One can either run a server from the RDDLSim Project, or run the Server class compiled from our source code with the following command
  ./run_server rddlfilename-or-dir portnumber num-rounds random-seed timeout 
  For example: 
  ./run_server Domains 2323 100 1 10000
  Note that the time out is the total time (in seconds) allowed to any client connects to it.
* Run SOGBOFA
  Type the following command
  ./run_sogbofa instance-name portnumber
  example:
  ./run_sogbofa traffic_inst_mdp__10 2323
* Results
  After finishing the requested number of runs, 6 files will be recorded in the bin/ folder, with the following names
  L_C_SOGBOFA_timeout_instance-name_Score
  L_C_SOGBOFA_timeout_instance-name_depthCounter
  L_C_SOGBOFA_timeout_instance-name_sizeCounter
  L_C_SOGBOFA_timeout_instance-name_rrCounter
  L_C_SOGBOFA_timeout_instance-name_seenCounter
  L_C_SOGBOFA_timeout_instance-name_updatesCounter
  which record respectively the average score of the runs, the average depth of the search, the average size of the computation graph, 
  the average number of radnom restarts, the average number of action evaluated, the average number of gradient updates.
  The Score file has three numbers, respectively the instance index, the average cumulative reward, and the standard deviation. The other
  files each has only one number inside.

-------------------------------------------------------------------------
RDDL FILES READY TO USE

Please find all domain and instance files used in the paper in the Domains/ folder.

-------------------------------------------------------------------------
OTHER WORK

Checkout the other project that uses the similar idea of "Aggregate Simulation", but work on Bayesian Inference. 
  Link (https://github.com/hcui01/AGS)

SOGBOFA is the MDP planner based on "aggregte simulation". 
  Link (https://github.com/hcui01/SOGBOFA)


