import java.math.*;
import java.util.*;

public class ExpBalanceTheoretical {

   public static void main(String[] args) {

      if (args.length != 4 && args.length != 6) {
         System.out.printf("Usage: java ExpBalanceTheoretical mean_d mean_w mean_t_h p_t_c [mu_low mu_high]\n" +
            "  e.g. java ExpBalanceTheoretical 80.0 78.0 0.01 0.01 100 130\n");
         return;
      }

      final double mD = Double.parseDouble(args[0]);
      final double mW = Double.parseDouble(args[1]);
      final double mTh = Double.parseDouble(args[2]);
      final double pTc = Double.parseDouble(args[3]);
      final int muLow;
      final int muHigh;

      if (args.length == 6) {
         muLow = Integer.parseInt(args[4]);
         muHigh = Integer.parseInt(args[5]);
      }
      else {
         muLow = 100;
         muHigh = 130;
      }

      final double gamma = 0.84;

      ExpTimeTheoretical timeObj = new ExpTimeTheoretical(mD, mW, mTh);

      System.out.println("\n mu    bal");
      System.out.println("------------------");

      for (double mu = muLow; mu < muHigh; mu++) {

         double expTime = timeObj.expVal(mu);

         double income = ((mD - mW) * expTime) / pTc;
         double losses = gamma * (mTh * expTime * mu) / pTc;

         double balance = income - losses;

         System.out.printf("%3.0f   %4.4f\n", mu, balance);
      }
   }
}