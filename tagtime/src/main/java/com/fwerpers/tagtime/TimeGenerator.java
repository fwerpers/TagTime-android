package com.fwerpers.tagtime;

import static com.fwerpers.tagtime.PingService.now;

/**
 * Created by FWerpers on 24/08/17.
 */

public class TimeGenerator {

    private static final long IA = 16807;
    private static final long IM = 2147483647;
    private static final long INITSEED = 666;

	/* *********************** *
	 * Random number generator * ***********************
	 */

    // Returns a random integer in [1,$IM-1]; changes $seed, ie, RNG state.
    // (This is ran0 from Numerical Recipes and has a period of ~2 billion.)
    private static long ran0(long seed) {
        long updatedSeed = IA * seed % IM;
        return(updatedSeed);
    }

    // Returns a U(0,1) random number.
    private static double ran01(long seed) {
        return ran0(seed) / (IM * 1.0);
    }

    // Returns a random number drawn from an exponential
    // distribution with mean gap. Gap is in minutes, we
    // want seconds, so multiply by 60.
    public static double exprand(int gap, long seed) {
        return -1 * gap * 60 * Math.log(ran01(seed));
    }

    // Takes previous ping time, returns random next ping time (unix time).
    // NB: this has the side effect of changing the RNG state ($seed)
    // and so should only be called once per next ping to calculate,
    // after calling prevping.
    public static long nextping(long prev, int gap, long seed) {
        if (Constants.DEBUG) return now() + 60;
        return Math.max(prev + 1, Math.round(prev + exprand(gap, seed)));
    }

    // Computes the last scheduled ping time before time t.
    public static long prevping(long t, int gap) {
        // Starting at the beginning of time, walk forward computing next pings
        // until the next ping is >= t.
        final int TUES = 1261198800; // some random time more recent than that..
        final int BOT = 1184083200; // start at the birth of timepie!
        long nxt = Constants.DEBUG ? TUES : BOT;
        long lst = nxt;
        while (nxt < t) {
            lst = nxt;
            nxt = nextping(nxt, gap, INITSEED);
        }
        return lst;
    }

}
