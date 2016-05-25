import java.math.*;
import java.util.*;

public class ExpTimeTheoretical {

   private double mD;
   private double mW;
   private double mTh;
   
   /* Intermediate values */
   private double disc;
   private double x1;
   private double x2;

   private double diff;
   private double prod;

   private double mThInv;

   public ExpTimeTheoretical(double mD, double mW, double mTh) {

      this.mD = mD;
      this.mW = mW;
      this.mTh = mTh;

      /* Precomputations */
      disc = Math.sqrt(Math.pow((mD + mW + mTh), 2) - 4*mD*mW);
      x1 = ((mD + mW + mTh) - disc)/(2*mD);
      x2 = ((mD + mW + mTh) + disc)/(2*mD);

      diff = x2 - x1;
      prod = x1 * x2;

      mThInv = 1.0 / mTh;

      /* Property: product of roots equals c/a */
      assert (Math.abs(prod - mW/mD) < 1E-5);
   }

   public double expectedTime(double mu) {

      double denom = ((mW*(x1-1) + mTh*x1) * Math.pow(x1, mu-1)) - ((mW*(x2-1) + mTh*x2) * Math.pow(x2, mu-1));
      return mThInv + (mThInv * mW * diff * Math.pow(prod, mu-1)) / denom;
   }

   public static void main(String[] args) {

      if (args.length != 3 && args.length != 6) {
         System.out.printf("Usage: java ExpTimeTheoretical mean_d mean_w mean_t_h [mu_low mu_high mu_inc]\n" +
            "  e.g. java ExpTimeTheoretical 79.0 78.0 0.01 10 600 10\n");
         return;
      }

      final double meanD = Double.parseDouble(args[0]);
      final double meanW = Double.parseDouble(args[1]);
      final double meanTh = Double.parseDouble(args[2]);       
      final int muLow;
      final int muHigh;
      final int muInc;

      if (args.length == 6) {
         muLow = Integer.parseInt(args[3]);
         muHigh = Integer.parseInt(args[4]);
         muInc = Integer.parseInt(args[5]);
      }
      else {
         muLow = 10;
         muHigh = 600;
         muInc = 10;
      }

      ExpTimeTheoretical timeObj = new ExpTimeTheoretical(meanD, meanW, meanTh);

      System.out.println("\n mu   exp_time");
      System.out.println("-----------------");

      for (int mu = muLow; mu < muHigh; mu += muInc) {
         double expTime = timeObj.expectedTime(mu);
         System.out.printf("%3d   %4.2f\n", mu, expTime);
      }
   }
}