import java.math.*;
import java.util.*;

public class ExpTimeEventDriven {

   private static Random r;

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

   public static void main(String[] args) {

      if (args.length != 3 && args.length != 6) {
         System.out.printf("Usage: java ExpTimeEventDriven mean_d mean_w mean_t_h [iterations mu_low mu_high]\n" +
            "  e.g. java ExpBalanceEventDriven 80.0 78.0 0.001 1000 5 500\n");
         return;
      }

      r = new Random();

      final double mD = Double.parseDouble(args[0]);
      final double mW = Double.parseDouble(args[1]);
      final double mTh = Double.parseDouble(args[2]);

      final int iterations;
      final int muLow;
      final int muHigh;

      if (args.length == 3) {
         iterations = 1000;
         muLow = 5;
         muHigh = 500;
      }
      else {
         iterations = Integer.parseInt(args[3]);
         muLow = Integer.parseInt(args[4]);
         muHigh = Integer.parseInt(args[5]);
      }

      System.out.println("\n mu  time");
      System.out.println("----------------");

      for (int mu = muLow; mu <= muHigh; mu++) {

         double cumulativeTime = 0.0;

         for (int i = 0; i < iterations; i++) {

            /* Times in seconds */
            double time = 0;                            // simulation time
            double timeToD = timeToEvent(mD/3600.0);    // time to next deposit
            double timeToW = timeToEvent(mW/3600.0);    // time to next withdrawal
            double timeToT = timeToEvent(mTh/3600.0);   // time to next hot wallet theft

            int balance = mu;

            while (balance > 0) {

               Event nextEvent = nextEvent(timeToD, timeToW, timeToT);

               if (nextEvent == Event.DEPOSIT) {
                  if (balance < mu) balance++;
                  time = timeToD;
                  timeToD += timeToEvent(mD/3600.0);
               }
               else if (nextEvent == Event.WITHDRAWAL) {
                  balance--;
                  time = timeToW;
                  timeToW += timeToEvent(mW/3600.0);
               }
               else {
                  balance = 0;
                  time = timeToT;
                  timeToT += timeToEvent(mTh/3600.0);
               }
            }

            cumulativeTime += (double)time/3600.0;
         }

         /* Print stats for current value of mu */
         System.out.printf("%d  %5.1f\n", mu, cumulativeTime/(double)iterations);
      }
   }
}
