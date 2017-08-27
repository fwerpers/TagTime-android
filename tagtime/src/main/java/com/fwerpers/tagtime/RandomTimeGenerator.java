package com.fwerpers.tagtime;

import static com.fwerpers.tagtime.PingService.now;

/**
 * Created by FWerpers on 25/08/17.
 */

public class RandomTimeGenerator {

    private static final long IA = 16807;
    private static final long IM = 2147483647;
    private static final long INITSEED = 666;

    private long mSeed;

    public RandomTimeGenerator(long seed) {
        mSeed = seed;
    }

    private void setSeed(long seed) {
        mSeed = seed;
    }

    public long getSeed() {
        return(mSeed);
    }

    /* *********************** *
	 * Random number generator * ***********************
	 */

    // Returns a random number drawn from an exponential
    // distribution with mean gap. Gap is in minutes, we
    // want seconds, so multiply by 60.
    public double exprand(int gap) {

        // Returns a random integer in [1,$IM-1]; changes $seed, ie, RNG state.
        // (This is ran0 from Numerical Recipes and has a period of ~2 billion.)
        long ran0 = IA * mSeed % IM;
        setSeed(ran0);

        // Returns a U(0,1) random number.
        double ran01 = ran0 / (IM * 1.0);

        return -1 * gap * 60 * Math.log(ran01);
    }

    // Takes previous ping time, returns random next ping time (unix time).
    // NB: this has the side effect of changing the RNG state ($seed)
    // and so should only be called once per next ping to calculate,
    // after calling prevping.
    public long nextping(long prev, int gap) {
        if (Constants.DEBUG) return now() + 60;
        return Math.max(prev + 1, Math.round(prev + exprand(gap)));
    }

    // Computes the last scheduled ping time before time t.
    public long prevping(long t, int gap) {
        mSeed = INITSEED;
        // Starting at the beginning of time, walk forward computing next pings
        // until the next ping is >= t.
        final int TUES = 1261198800; // some random time more recent than that..
        final int BOT = 1184083200; // start at the birth of timepie!
        long nxt = Constants.DEBUG ? TUES : BOT;
        long lst = nxt;
        long lstseed = mSeed;
        while (nxt < t) {
            lst = nxt;
            lstseed = mSeed;
            nxt = nextping(nxt, gap);
        }
        mSeed = lstseed;
        return lst;
    }

    public long recalculateNextTime(long time, int gap) {
        return(nextping(prevping(time, gap), gap));
    }
}
