import java.math.*;
import java.util.*;

public class ExpBalanceEventDriven {

   private static Random r;

   private int hotBalance;
   private int coldBalance;

   private int hotThefts;
   private int coldThefts;

   private int hotColdTransfers;
   private int coldHotTransfers;

   private enum Event {

      DEPOSIT, WITHDRAWAL, THEFT
   }

   /* Given expected number of events in a given unit of time,
   returns time interval to next event (exponential distribution). */
   private static double timeToEvent(double mean) {

      return (-1.0 * Math.log(r.nextDouble()) / mean);
   }

   /* Given times to next deposit, withdrawal, and hot wallet theft,
   returns most imminent Event. */
   private static Event nextEvent(double nextD, double nextW, double nextT) {

      if (nextD <= nextW && nextD <= nextT) {
         return Event.DEPOSIT;
      }
      else if (nextW <= nextD && nextW <= nextT) {
         return Event.WITHDRAWAL;
      }
      else {
         return Event.THEFT;
      }
   }
   
   /* Refills hot wallet if empty. Cold wallet is robbed with
   probability pTheft after transaction. Return 1 if cold wallet
   theft occurred, 0 if not. */
   private boolean refillHot(int mu, double pTheft) {

      /* if cold wallet does not have enough funds to refill hot, alert */
      /*if (this.coldBalance < mu)
         System.out.println("************ BANKRUPTCY!!! ************ ");*/

      /* Refill hot */
      this.coldBalance -= mu;
      this.hotBalance = mu;
      this.coldHotTransfers++;

      /* Robbery with probability pTheft */
      double p = r.nextDouble();
      if (p <= pTheft) {
         this.coldBalance = 0;
         this.coldThefts++;
         return true;
      }

      return false;
   }

   public static void main(String[] args) {

      r = new Random();

      /* simulation parameters ************/
      final double mD = 79;
      final double mW = 78;
      final double mTh = 0.01;       /* expected: 1 theft every 100 hours     */
      final double pTc = 0.01;       /* expected: 1 theft every 100 accesses  */

      /* in seconds */
      long timeSpan = 3600 * 20000;
      /************************************/

      System.out.println("\nmD: " + mD);
      System.out.println("mu  exp_bal run_time");

      for (int mu = 65; mu <= 85; mu++) {

         /* testing parameters/stats *********/
         int iterations = 1000;

         int totColdHot = 0;
         int totHotCold = 0;

         int totHotThefts = 0;
         int totColdThefts = 0;

         int totColdBal = 0;
         int totHotBal = 0;
         int totBalance = 0;
         /************************************/

         /* simulation parameters ************/
         int startingHot = mu;
         int startingCold = 0;
         /************************************/

         long t1 = System.currentTimeMillis();

         /* tests */
         for (int j = 0; j < iterations; j++) {

            ExpBalanceEventDriven sim = new ExpBalanceEventDriven();

            sim.coldHotTransfers = 0;
            sim.hotColdTransfers = 0;

            sim.hotThefts = 0;
            sim.coldThefts = 0;

            sim.hotBalance = startingHot;
            sim.coldBalance = startingCold;

            /* in seconds */
            double time = 0.0;

            double nextD = timeToEvent(mD/3600.0);    // next deposit
            double nextW = timeToEvent(mW/3600.0);    // next withdrawal
            double nextT = timeToEvent(mTh/3600.0);   // next hot wallet theft

            while (time < timeSpan) {

               Event nextEvent = nextEvent(nextD, nextW, nextT);

               if (nextEvent == Event.DEPOSIT) {

                  //System.out.printf("%.2f Deposit\n", time);

                  if (sim.hotBalance < mu) {
                     sim.hotBalance++;
                  }
                  else {
                     sim.coldBalance++;
                     sim.hotColdTransfers++;
                  }
                  
                  time = nextD;
                  nextD += timeToEvent(mD/3600.0);
               }

               else if (nextEvent == Event.WITHDRAWAL) {

                  //System.out.printf("%.2f Withdrawal\n", time);

                  sim.hotBalance--;

                  time = nextW;
                  nextW += timeToEvent(mW/3600.0);
               }

               else {

                  //System.out.printf("%.2f Hot Theft!\n", time);

                  sim.hotThefts++;
                  sim.hotBalance = 0;

                  time = nextT;
                  nextT += timeToEvent(mTh/3600.0);
               }

               /* hot wallet refill */
               if (sim.hotBalance <= 0) {
                  //System.out.printf("%.2f Hot Refill\n", time);

                  if (sim.refillHot(mu, pTc)) {
                     //System.out.printf("%.2f Cold Theft!\n", time);
                  }
               }

               //System.out.println("Hot wallet balance: " + sim.hotBalance);
               //System.out.println("Cold wallet balance: " + sim.coldBalance);
            }

            /* System.out.println("\nStats for Trial " + j + ":");
            System.out.println("Hot -> cold transfers: " + sim.hotColdTransfers);
            System.out.println("Cold -> hot transfers: " + sim.coldHotTransfers);

            System.out.println("Hot w. thefts: " + sim.hotThefts);
            System.out.println("Cold w. thefts: " + sim.coldThefts);

            System.out.println("Balance: " + (sim.hotBalance - startingHot + sim.coldBalance - startingCold));
            System.out.println(); */

            /* Record cumulative statistics */
            totHotCold += sim.hotColdTransfers;
            totColdHot += sim.coldHotTransfers;

            totHotThefts += sim.hotThefts;
            totColdThefts += sim.coldThefts;

            totColdBal += sim.coldBalance;
            totHotBal += sim.hotBalance;

            totBalance += sim.hotBalance + sim.coldBalance;
         }

         long t2 = System.currentTimeMillis();
         double timeElapsed = (t2 - t1)/1000.0; 

         System.out.println(mu + "  " + (double)totBalance/(double)iterations + " " + timeElapsed);

         //System.out.println("\nOTHER CUMULATIVE STATISTICS ******************\n");
         
         System.out.println("Avg h->c transfers: " + (double)totHotCold/(double)iterations);
         System.out.println("Avg c->h transfers: " + (double)totColdHot/(double)iterations);

         System.out.println("Avg h. w. thefts: " + (double)totHotThefts/(double)iterations);
         System.out.println("Avg c. w. thefts: " + (double)totColdThefts/(double)iterations);

         System.out.println("Avg h. w. balance: " + (double)totHotBal/(double)iterations);
         System.out.println("Avg c. w. balance: " + (double)totColdBal/(double)iterations);
         System.out.println();
      }

   }
}

