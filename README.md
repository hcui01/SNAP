-------------------------------------------------------------------------
OVERVIEW

SNAP is a state-of-the-art stochastic online planning algorithm for POMDPs, corresponding to the paper

H. Cui and R. Khardon, Sampling Networks and Aggregate Simulation for Online POMDP Planning (http://homes.sice.indiana.edu/rkhardon/PUB/aaai15algebraic.pdf), Proceedings of the Conference on Neural Information Processing Systems (NeurIPS), 2019.

SNAP uses the idea of "Aggregate Simulation" to build a computation graph which symbolically approximates the Q value of actions given the current state, and searches the action space very efficiently using gradient updates. It supports various action constraints defined in first-order predicates. It directly works with POMDP domains defined in RDDL, and is implemented based on the original code of the RDDL simulator (https://github.com/ssanner/rddlsim).
   
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

You can find other papers of interest at Hao Cui's personal page
   https://sites.google.com/view/hao-cui/home
   
-------------------------------------------------------------------------
HOW TO USE

* Prerequisites: Java SE 1.8 or higher
* Compile
  In the root directory, type command ./compile
* Run the server
  One can either run a server from the RDDLSim Project, or run the Server class compiled from our source code with the following command
  ./run_server rddlfilename-or-dir portnumber num-rounds random-seed timeout 
  For example: 
  ./run_server Domains 2323 100 1 8000
  Note that the time out is the total time (in seconds) allowed to any client connects to it.
* Run SNAP
  Type the following command
  ./run_SNAP instance-name portnumber Algorithm
  where the algorithm can be either Y_F for SNAP with all observations enumarated, Y_F_Sample for SNAP with sampling network, and RandomConcurrentPolicy for random.
  example:
  ./run_SNAP traffic_inst_pomdp__1 2323 Y_F_Sample

-------------------------------------------------------------------------
RDDL FILES READY TO USE

Please find all domain and instance files used in the paper in the Domains/ folder.

-------------------------------------------------------------------------
BASE LINES

To reproduce results in NeurIPS 2019 paper with other systems please
download and run as follows.

DESPOT: The source code is at
    https://github.com/AdaCompNUS/despot/
Compile the code per instructions and run with default parameters.

POMCP: The source code is at

http://bigbird.comp.nus.edu.sg/pmwiki/farm/appl/uploads/Main/POMDPX_NUS.zip

We use the code with input given by a POMDPX domain description which is
obtained from the RDDL with the translator available at

http://bigbird.comp.nus.edu.sg/pmwiki/farm/appl/uploads/Main/ippc_2014_translator

Compile the code per instructions and run with default parameters.

-------------------------------------------------------------------------
OTHER WORK

Checkout the other project that uses the similar idea of "Aggregate Simulation", but work on Bayesian Inference. 
  Link (https://github.com/hcui01/AGS)

SOGBOFA is the MDP planner based on "aggregte simulation". 
  Link (https://github.com/hcui01/SOGBOFA)


