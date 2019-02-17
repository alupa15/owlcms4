/***
 * Copyright (c) 2018-2019 Jean-François Lamy
 * 
 * This software is licensed under the the Affero GNU License amended with the
 * Commons Clause.
 * See https://redislabs.com/wp-content/uploads/2018/10/Commons-Clause-White-Paper.pdf
 */
package org.ledocte.owlcms.data.athleteSort;

import java.util.Comparator;

import org.ledocte.owlcms.data.athlete.Athlete;

/**
 * This comparator is used to highlight the athletes that have lifted recently, and are likely to request changes to the automatic
 * progression. It simply sorts according to time stamp, if available. Else lot number is used.
 * 
 * @author jflamy
 * 
 */
public class LiftTimeStampComparator extends AbstractLifterComparator implements Comparator<Athlete> {

    /**
     * Instantiates a new lift time stamp comparator.
     */
    public LiftTimeStampComparator() {
    }

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(Athlete lifter1, Athlete lifter2) {
        int compare = 0;

        compare = comparePreviousLiftOrder(lifter1, lifter2);
        if (compare != 0)
            return -compare;

        compare = compareLotNumber(lifter1, lifter2);
        if (compare != 0)
            return compare;

        return compare;
    }

}
