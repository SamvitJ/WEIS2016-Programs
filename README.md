**Determining an Optimal Threshold on the Online Reserves of a Bitcoin Exchange**

This repository contains the source code for a [paper](http://weis2016.econinfosec.org/wp-content/uploads/sites/2/2016/05/WEIS_2016_paper_68-2.pdf) on Bitcoin storage presented at the 15th Annual Workshop on the Economics of Information Security ([WEIS 2016](http://weis2016.econinfosec.org/)), held at UC Berkeley.

The key focus of the paper is to understand the behavior of a hot/cold wallet system which supports customer deposits and withdrawals, and suffers from periodic hot and cold wallet thefts, over a long time interval [0, T]. Specifically, we look at the net balance of the wallets at T as a function of the ceiling capacity of the hot wallet, with the goal of finding the optimal value for this threshold.

The code is divided into two directories - Theory and Simulations - each of which contains two fairly simple Java programs.

[ExpBalanceTheoretical.java](https://github.com/SamvitJ/WEIS2016-Programs/blob/master/Theory/ExpBalanceTheoretical.java) analytically computes the net balance as a function of various system parameters (e.g. deposit rate, hot wallet theft rate), provided as command line arguments. These results can be compared to those obtained via an event-driven simulation, [ExpBalanceEventDriven.java](https://github.com/SamvitJ/WEIS2016-Programs/blob/master/Simulations/ExpBalanceEventDriven.java), which computes the same value by repeatedly drawing the waiting times to and executing the next deposit, withdrawal, and hot wallet theft events in a loop, until a time counter reaches T.

The other pair of programs, [ExpTimeTheoretical.java](https://github.com/SamvitJ/WEIS2016-Programs/blob/master/Theory/ExpTimeTheoretical.java) and [ExpTimeEventDriven.java](https://github.com/SamvitJ/WEIS2016-Programs/blob/master/Simulations/ExpTimeEventDriven.java), output an intermediate result - the expected time to an empty hot wallet. This value is used in our formula for the expected balance of the wallets, and is a key subcomponent of our theory.

To compile any program (e.g. ExpBalanceEventDriven.java), run
```
javac ExpBalanceEventDriven.java
```

To run, provide the appropriate command line arguments specified in the usage message, e.g.
```
java ExpBalanceEventDriven 80.0 78.0 0.01 0.01
```

For more information, please look at Sections 2 (Problem Formulation) and 5 (Approach) of our paper. Feel free to send me an email at samvitj AT princeton DOT edu with any questions.

Authors: Samvit Jain, Edward Felten, Steven Goldfeder.  
Institution: Department of Computer Science, Princeton University.
