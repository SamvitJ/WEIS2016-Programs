import java.math.*;
import java.util.*;
import java.lang.*;
import java.security.SecureRandom;

public class ExpTimeEventDriven
{
   private static Random r;

   private enum Event
   {
      DEPOSIT, WITHDRAWAL, THEFT
   }

   /* Given expected number of events in a given unit of time,
   returns time interval to next event (exponential distribution). */
   private static double timeToEvent(double mean)
   {
      double p = r.nextDouble();
      return (-1.0 * Math.log(p) / mean);
   }

   /* Given times to next deposit, withdrawal, and hot wallet theft,
   returns most imminent Event. */
   private static Event nextEvent(double nextD, double nextW, double nextT)
   {
      if (nextD <= nextW && nextD <= nextT)
      {
         return Event.DEPOSIT;
      }
      else if (nextW <= nextD && nextW <= nextT)
      {
         return Event.WITHDRAWAL;
      }
      else
      {
         return Event.THEFT;
      }
   }

   public static void main(String[] args)
   {
      r = new Random();

      final double[] mDTest = {79,  80,  85, 100,   78,   77,   76,   71,   70,   50};
      final double[] muLim = {800, 600, 500, 500, 2500, 3000, 3000, 3000, 3000, 3000};

      final double mW = 78.0;
      final double mTh = 0.001;                 // expected: 1 theft every 1000 hours  
      
      double mD;  
      int mu;                                   // hot wallet threshold

      for (int k = 0; k < mDTest.length; k++) 
      {
         mD = mDTest[k];
         System.out.println("\nmD: " + mD);
         
         for (mu = 5; mu < muLim[k]; mu++)
         {
            int iterations = 1000;

            double cumulativeTime = 0.0;

            /* stats ****** */
            /* hourly rates */
            double cumDeposits = 0;
            double cumWithdrawals = 0;

            double cumThefts = 0;
            double cumTheftTime = 0;
            /* ************ */

            long t1 = System.currentTimeMillis();

            for (int i = 0; i < iterations; i++)
            {
               /* time in seconds */
               double nextD = timeToEvent(mD/3600.0);    // next deposit
               double nextW = timeToEvent(mW/3600.0);    // next withdrawal
               double nextT = timeToEvent(mTh/3600.0);   // next hot wallet theft

               /*System.out.println("firstD: " + nextD);
               System.out.println("firstW: " + nextW);
               System.out.println("firstT: " + nextT);*/

               int reserves = 0;
               int balance = mu;
               double time = 0;

               int deposits = 0;
               int withdrawals = 0;
               int thefts = 0;
               double theftTime = 0;

               while (balance > 0)
               {
                  //System.out.println("time: " + time + " bal: " + balance);

                  Event nextEvent = nextEvent(nextD, nextW, nextT);

                  if (nextEvent == Event.DEPOSIT)
                  {
                     //System.out.println("Deposit"); 
                     deposits++;

                     if (balance < mu) balance++;
                     else reserves++;

                     time = nextD;
                     nextD += timeToEvent(mD/3600.0);
                  }

                  else if (nextEvent == Event.WITHDRAWAL)
                  {
                     //System.out.println("Withdrawal"); 
                     withdrawals++;

                     balance--;

                     time = nextW;
                     nextW += timeToEvent(mW/3600.0);
                  }

                  else
                  {
                     //System.out.println("Theft!"); 
                     thefts++;
                     theftTime = time;

                     balance = 0;

                     time = nextT;
                     nextT += timeToEvent(mTh/3600.0);

                     break;
                  }
               }

               double hoursToEmpty = (double)time/3600.0;
               //System.out.println("Iteration: " + i + " Time: " + hoursToEmpty + " Reserves: " + reserves);
               //System.out.println("Thefts: " + thefts + " Deposits: " + deposits + " Withdrawals: " + withdrawals);

               cumulativeTime += hoursToEmpty;

               /* stats ****** */
               /* hourly rates */
               cumDeposits += (double)deposits/hoursToEmpty;
               cumWithdrawals += (double)withdrawals/hoursToEmpty;

               cumThefts += (double)thefts;
               cumTheftTime += (double)theftTime/3600.0;
               /* ************ */
            }

            long t2 = System.currentTimeMillis();
            double timeElapsed = (t2 - t1)/1000.0; 

            //System.out.println("mu: " + mu + " Average: " + cumulativeTime/(double)iterations);
            System.out.println(mu + " " + cumulativeTime/(double)iterations + " " + timeElapsed);

            /* stats ****** */
            /*System.out.println("Thefts: " + cumThefts/(double)iterations + 
               " Deposits: " + cumDeposits/(double)iterations + 
               " Withdrawals: " + cumWithdrawals/(double)iterations + 
               " Theft time: " + cumTheftTime/(double)iterations);
            /* ************ */
         }
      }
   }
}
