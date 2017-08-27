package com.fwerpers.tagtime;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by FWerpers on 27/08/17.
 */


public class RandomTimeGeneratorTest {

    @Test
    public void testRecalculateFromBeginning() {
        long inputSeed = -1;

        // Recorded from PingService
        long launchTime = 1503817871;
        long outputSeed = 217767729;
        long outputNext = 1503818155;

        RandomTimeGenerator generator = new RandomTimeGenerator(inputSeed);
        long next = generator.recalculateNextTime(launchTime, 5);
        long seed = generator.getSeed();

        assertEquals(outputSeed, seed);
        assertEquals(outputNext, next);
    }

}