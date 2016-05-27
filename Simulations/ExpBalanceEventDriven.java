import java.math.*;
import java.util.*;

public class ExpBalanceEventDriven {

   private static Random r;

   private int mu;

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
   public static double timeToEvent(double mean) {

      return (-1.0 * Math.log(r.nextDouble()) / mean);
   }

   /* Given times to next deposit, withdrawal, and hot wallet theft,
   returns most imminent Event. */
   public static Event nextEvent(double nextD, double nextW, double nextT) {

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

   public ExpBalanceEventDriven(int threshold) {

      this.mu = threshold;

      this.coldHotTransfers = 0;
      this.hotColdTransfers = 0;

      this.hotThefts = 0;
      this.coldThefts = 0;

      this.hotBalance = mu;
      this.coldBalance = 0;
   }
   
   /* Makes deposit to hot wallet if available capacity,
   else deposits in cold wallet. */
   public void deposit() {

      if (hotBalance < mu) {
         hotBalance++;
      }
      else {
         coldBalance++;
         hotColdTransfers++;
      }
   }

   /* Makes withdrawal from hot wallet */
   public void withdraw() {

      hotBalance--;
   }

   /* Empties hot wallet */
   public void theftHot() {

      hotThefts++;
      hotBalance = 0;
   }

   /* Refills hot wallet if empty. Cold wallet is robbed with
   probability pTheft after transaction. */
   public void refillHot(double pTheft) {

      /* Refill hot wallet */
      if (this.coldBalance < mu) {
         this.coldBalance = 0;
      }
      else {
         this.coldBalance -= mu;
      }
      hotBalance = mu;
      coldHotTransfers++;

      /* Robbery with probability pTheft */
      double p = r.nextDouble();

      if (p <= pTheft) {
         coldBalance = 0;
         coldThefts++;
      }
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

         /* Statistics */
         int totColdHot = 0;
         int totHotCold = 0;

         int totHotThefts = 0;
         int totColdThefts = 0;

         int totColdBal = 0;
         int totHotBal = 0;
         int totBalance = 0;

         for (int j = 0; j < iterations; j++) {

            ExpBalanceEventDriven sim = new ExpBalanceEventDriven(mu);

            /* Times in seconds */
            double time = 0.0;                          // simulation time
            double timeToD = timeToEvent(mD/3600.0);    // time to next deposit
            double timeToW = timeToEvent(mW/3600.0);    // time to next withdrawal
            double timeToT = timeToEvent(mTh/3600.0);   // time to next hot wallet theft

            while (time < timespan) {

               Event nextEvent = nextEvent(timeToD, timeToW, timeToT);
               switch (nextEvent) {
                  case DEPOSIT:
                     sim.deposit();
                     time = timeToD;
                     timeToD += timeToEvent(mD/3600.0);
                     break;
                  case WITHDRAWAL:
                     sim.withdraw();
                     time = timeToW;
                     timeToW += timeToEvent(mW/3600.0);
                  case THEFT:
                     sim.theftHot();
                     time = timeToT;
                     timeToT += timeToEvent(mTh/3600.0);
                  default:
                     break;
               }

               /* Hot wallet refill */
               if (sim.hotBalance <= 0) {
                  sim.refillHot(pTc);
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

