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
   probability pTheft after transaction.
   Returns true if cold wallet theft occurred, false if not. */
   private boolean refillHot(int mu, double pTheft) {

      /* Refill hot wallet */
      if (this.coldBalance < mu) {
         this.coldBalance = 0;
      } else {
         this.coldBalance -= mu;
      }
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

      if (args.length != 4 && args.length != 8) {
         System.out.printf("Usage: java ExpBalanceEventDriven mean_d mean_w mean_t_h p_t_c [timespan iterations mu_low mu_high]\n" +
            "  e.g. java ExpBalanceEventDriven 80.0 78.0 0.01 0.01 20000 1000 100 130\n");
         return;
      }

      r = new Random();

      final double mD = Double.parseDouble(args[0]);
      final double mW = Double.parseDouble(args[1]);
      final double mTh = Double.parseDouble(args[2]);
      final double pTc = Double.parseDouble(args[3]);

      final long timespan;
      final int iterations;
      final int muLow;
      final int muHigh;

      if (args.length == 4) {
         timespan = 3600 * 20000;
         iterations = 1000;
         muLow = 100;
         muHigh = 130;
      }
      else {
         timespan = 3600 * Integer.parseInt(args[4]);
         iterations = Integer.parseInt(args[5]);
         muLow = Integer.parseInt(args[6]);
         muHigh = Integer.parseInt(args[7]);
      }

      System.out.println("\n mu  exp_bal");
      System.out.println("----------------");

      for (int mu = muLow; mu <= muHigh; mu++) {

         /* Statistics *********/
         int totColdHot = 0;
         int totHotCold = 0;

         int totHotThefts = 0;
         int totColdThefts = 0;

         int totColdBal = 0;
         int totHotBal = 0;
         int totBalance = 0;

         /* Other parameters ************/
         int startingHot = mu;
         int startingCold = 0;

         long t1 = System.currentTimeMillis();

         for (int j = 0; j < iterations; j++) {

            ExpBalanceEventDriven sim = new ExpBalanceEventDriven();

            sim.coldHotTransfers = 0;
            sim.hotColdTransfers = 0;

            sim.hotThefts = 0;
            sim.coldThefts = 0;

            sim.hotBalance = startingHot;
            sim.coldBalance = startingCold;

            double time = 0.0;      /* in seconds */

            double timeToD = timeToEvent(mD/3600.0);    // next deposit
            double timeToW = timeToEvent(mW/3600.0);    // next withdrawal
            double timeToT = timeToEvent(mTh/3600.0);   // next hot wallet theft

            while (time < timespan) {

               Event nextEvent = nextEvent(timeToD, timeToW, timeToT);

               if (nextEvent == Event.DEPOSIT) {

                  if (sim.hotBalance < mu) {
                     sim.hotBalance++;
                  } else {
                     sim.coldBalance++;
                     sim.hotColdTransfers++;
                  }
                  
                  time = timeToD;
                  timeToD += timeToEvent(mD/3600.0);

               } else if (nextEvent == Event.WITHDRAWAL) {

                  sim.hotBalance--;

                  time = timeToW;
                  timeToW += timeToEvent(mW/3600.0);

               } else {

                  sim.hotThefts++;
                  sim.hotBalance = 0;

                  time = timeToT;
                  timeToT += timeToEvent(mTh/3600.0);
               }

               /* Hot wallet refill */
               if (sim.hotBalance <= 0) {

                  sim.refillHot(mu, pTc);
               }
            }

            /* Record cumulative statistics */
            totHotCold += sim.hotColdTransfers;
            totColdHot += sim.coldHotTransfers;

            totHotThefts += sim.hotThefts;
            totColdThefts += sim.coldThefts;

            totColdBal += sim.coldBalance;
            totHotBal += sim.hotBalance;

            totBalance += sim.hotBalance + sim.coldBalance;
         }

         /* Print stats for current value of mu */
         System.out.printf("%d  %5.1f\n", mu, (double)totBalance/(double)iterations);
         
         /* Other cumulative statistics
         System.out.println("Avg h->c transfers: " + (double)totHotCold/(double)iterations);
         System.out.println("Avg c->h transfers: " + (double)totColdHot/(double)iterations);

         System.out.println("Avg h.w. thefts: " + (double)totHotThefts/(double)iterations);
         System.out.println("Avg c.w. thefts: " + (double)totColdThefts/(double)iterations);

         System.out.println("Avg h.w. balance: " + (double)totHotBal/(double)iterations);
         System.out.println("Avg c.w. balance: " + (double)totColdBal/(double)iterations);
         System.out.println(); */
      }

   }
}

