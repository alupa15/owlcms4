/*
 * Copyright 2009-2012, Jean-François Lamy
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.ledocte.owlcms.tests;

import static org.junit.Assert.assertEquals;
import static org.ledocte.owlcms.tests.AllTests.assertEqualsToReferenceFile;

import java.util.Collections;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ledocte.owlcms.data.athlete.Athlete;
import org.ledocte.owlcms.data.athlete.AthleteRepository;
import org.ledocte.owlcms.data.athleteSort.AthleteSorter;
import org.ledocte.owlcms.data.jpa.JPAService;
import org.ledocte.owlcms.state.FOPEvent;
import org.ledocte.owlcms.state.FieldOfPlayState;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class TwoMinutesRuleTest {
	private static Level LoggerLevel = Level.INFO;
	final static Logger logger = (Logger) LoggerFactory.getLogger(TwoMinutesRuleTest.class);
	private List<Athlete> athletes;

	@BeforeClass
	public static void setupTests() {
		JPAService.init(true);
	}

	@AfterClass
	public static void tearDownTests() {
		JPAService.close();
	}

	@Before
	public void setupTest() {
		// for this test, the initial data does not include body weights, so we use
		// false
		// on the constructor to disable exclusion of incomplete data.
		athletes = AthleteRepository.findAll();
		logger.setLevel(LoggerLevel);
	}

	@Test
	public void initialCheck() {
		final String resName = "/initialCheck.txt"; //$NON-NLS-1$
		AthleteSorter.assignLotNumbers(athletes);
		AthleteSorter.assignStartNumbers(athletes);

		Collections.shuffle(athletes);

		List<Athlete> sorted = AthleteSorter.liftingOrderCopy(athletes);
		final String actual = AllTests.shortDump(sorted);
		assertEqualsToReferenceFile(resName, actual);
	}

	@Test
	public void liftSequence3() throws InterruptedException {
		AthleteSorter.assignLotNumbers(athletes);

		final Athlete schneiderF = athletes.get(0);
		final Athlete simpsonR = athletes.get(1);

		// simulate initial declaration at weigh-in
		schneiderF.setSnatch1Declaration(Integer.toString(60));
		simpsonR.setSnatch1Declaration(Integer.toString(60));
		schneiderF.setCleanJerk1Declaration(Integer.toString(80));
		simpsonR.setCleanJerk1Declaration(Integer.toString(82));

		// hide non-athletes
		AthleteSorter.liftingOrder(athletes);
		final int size = athletes.size();
		for (int i = 2; i < size; i++)
			athletes.remove(2);

		FieldOfPlayState.getLogger()
			.setLevel(LoggerLevel);
		FieldOfPlayState fopState = new FieldOfPlayState(athletes, new MockCountdownTimer());
		fopState.setStartTimeAutomatically(true);
		EventBus fopBus = fopState.getEventBus();

		// competition start
		assertEquals(60000, fopState.timeAllowed());
		logger.debug("\n{}", AllTests.shortDump(fopState.getLifters()));

		// schneiderF is called with initial weight
		Athlete curLifter = fopState.getCurAthlete();
		Athlete previousLifter = fopState.getPreviousAthlete();
		assertEquals(schneiderF, curLifter);
		assertEquals(null, previousLifter);
		successfulLift(fopBus, curLifter);
		
		logger.debug("\n{}", AllTests.shortDump(fopState.getLifters()));
		// first is now simpsonR ; he has declared 60kg
		curLifter = fopState.getCurAthlete();
		previousLifter = fopState.getPreviousAthlete();
		assertEquals(simpsonR, curLifter);
		assertEquals(schneiderF, previousLifter);
		assertEquals(60000, fopState.timeAllowed());

		// ... but simpsonR changes to 62 before being called by announcer (time not restarted)
		declaration(curLifter, "62", fopBus); //$NON-NLS-1$
		logger.info("declaration by {}: {}", curLifter, "62"); //$NON-NLS-1$ //$NON-NLS-2$
		logger.debug("\n{}", AllTests.shortDump(fopState.getLifters()));

		// so now schneider should be back on top at 61, with two minutes because
		// there was no time started.
		curLifter = fopState.getCurAthlete();
		previousLifter = fopState.getPreviousAthlete();
		assertEquals(schneiderF, curLifter);
		assertEquals(schneiderF, previousLifter);
		assertEquals(120000, fopState.timeAllowed());
		successfulLift(fopBus, curLifter);

		// schneider has lifted 62, is now simpson's turn, he should NOT have 2
		// minutes
		curLifter = fopState.getCurAthlete();
		assertEquals(simpsonR, curLifter);
		assertEquals(60000, fopState.timeAllowed());
		failedLift(fopBus, curLifter);

		// still simpson because 2nd try and schneider is at 3rd.
		curLifter = fopState.getCurAthlete();
		assertEquals(simpsonR, curLifter);
		assertEquals(120000, fopState.timeAllowed());
		// simpson is called again with two minutes
		logger.info("calling lifter: {}", curLifter); //$NON-NLS-1$
		fopBus.post(new FOPEvent.AthleteAnnounced()); // this starts logical time
		assertEquals(FieldOfPlayState.State.TIME_RUNNING ,fopState.getState()) ;
		// but simpson now asks for more; weight change should stop clock.
		declaration(curLifter, "67", fopBus); //$NON-NLS-1$
		logger.info("declaration by {}: {}", curLifter, "67"); //$NON-NLS-1$ //$NON-NLS-2$

		// schneider does not get 2 minutes.
		curLifter = fopState.getCurAthlete();
		assertEquals(schneiderF, curLifter);
		assertEquals(60000, fopState.timeAllowed());
		// schneider is called
		logger.info("calling lifter: {}", curLifter); //$NON-NLS-1$
		fopBus.post(new FOPEvent.AthleteAnnounced()); // this starts logical time
		assertEquals(FieldOfPlayState.State.TIME_RUNNING, fopState.getState());
		// but asks for more weight -- the following stops time.
		declaration(curLifter, "65", fopBus); //$NON-NLS-1$
		assertEquals(FieldOfPlayState.State.CURRENT_ATHLETE_DISPLAYED, fopState.getState());
		int remainingTime = fopState.getTimer().getTimeRemaining();

		// at this point, if schneider is called again, he should get the remaining time.
		curLifter = fopState.getCurAthlete();
		assertEquals(schneiderF, curLifter);
		assertEquals(remainingTime, fopState.timeAllowed());
	}

	@Test
	public void liftSequence4() throws InterruptedException {
		AthleteSorter.assignLotNumbers(athletes);

		final Athlete schneiderF = athletes.get(0);
		final Athlete simpsonR = athletes.get(1);

		// simulate initial declaration at weigh-in
		schneiderF.setSnatch1Declaration(Integer.toString(60));
		simpsonR.setSnatch1Declaration(Integer.toString(65));
		schneiderF.setCleanJerk1Declaration(Integer.toString(80));
		simpsonR.setCleanJerk1Declaration(Integer.toString(85));

		// hide non-athletes
		AthleteSorter.liftingOrder(athletes);
		final int size = athletes.size();
		for (int i = 2; i < size; i++)
			athletes.remove(2);
		FieldOfPlayState fopState = new FieldOfPlayState(athletes, new MockCountdownTimer());
		fopState.setStartTimeAutomatically(true);
		EventBus fopBus = fopState.getEventBus();

		// competition start
		assertEquals(60000, fopState.timeAllowed());
		assertEquals(60000, fopState.timeAllowed());

		// schneiderF snatch1
		Athlete curLifter = fopState.getCurAthlete();
		assertEquals(schneiderF, curLifter);
		successfulLift(fopBus, curLifter);

		// schneiderF snatch2
		curLifter = fopState.getCurAthlete();
		assertEquals(schneiderF, curLifter);
		assertEquals(120000, fopState.timeAllowed());
		successfulLift(fopBus, curLifter);

		// schneiderF snatch3
		curLifter = fopState.getCurAthlete();
		assertEquals(schneiderF, curLifter);
		assertEquals(120000, fopState.timeAllowed());
		successfulLift(fopBus, curLifter);

		// simpsonR snatch1
		curLifter = fopState.getCurAthlete();
		assertEquals(simpsonR, curLifter);
		assertEquals(60000, fopState.timeAllowed());
		successfulLift(fopBus, curLifter);

		// simpsonR snatch2
		curLifter = fopState.getCurAthlete();
		assertEquals(simpsonR, curLifter);
		assertEquals(120000, fopState.timeAllowed());
		successfulLift(fopBus, curLifter);

		// simpsonR snatch3
		curLifter = fopState.getCurAthlete();
		assertEquals(simpsonR, curLifter);
		assertEquals(120000, fopState.timeAllowed());
		successfulLift(fopBus, curLifter);

		// schneiderF cj1
		curLifter = fopState.getCurAthlete();
		assertEquals(schneiderF, curLifter);
		assertEquals(60000, fopState.timeAllowed());
		successfulLift(fopBus, curLifter);

		// schneiderF cj2
		curLifter = fopState.getCurAthlete();
		assertEquals(schneiderF, curLifter);
		assertEquals(schneiderF, fopState.getPreviousAthlete());
		assertEquals(120000, fopState.timeAllowed());
		successfulLift(fopBus, curLifter);

		// schneiderF cj3
		curLifter = fopState.getCurAthlete();
		assertEquals(schneiderF, curLifter);
		assertEquals(120000, fopState.timeAllowed());
		successfulLift(fopBus, curLifter);

		// simpsonR cj1
		curLifter = fopState.getCurAthlete();
		assertEquals(simpsonR, curLifter);
		assertEquals(60000, fopState.timeAllowed());
		successfulLift(fopBus, curLifter);

		// simpsonR cj2
		curLifter = fopState.getCurAthlete();
		assertEquals(simpsonR, curLifter);
		assertEquals(120000, fopState.timeAllowed());
		successfulLift(fopBus, curLifter);

		// simpsonR cj3
		curLifter = fopState.getCurAthlete();
		assertEquals(simpsonR, curLifter);
		assertEquals(120000, fopState.timeAllowed());
		successfulLift(fopBus, curLifter);
	}

	/**
	 * @param lifter
	 * @param weight
	 * @param eventBus TODO
	 */
	private void declaration(final Athlete lifter, final String weight, EventBus eventBus) {
		switch (lifter.getAttemptsDone() + 1) {
		case 1:
			lifter.setSnatch1Declaration(weight);
			break;
		case 2:
			lifter.setSnatch2Declaration(weight);
			break;
		case 3:
			lifter.setSnatch3Declaration(weight);
			break;
		case 4:
			lifter.setCleanJerk1Declaration(weight);
			break;
		case 5:
			lifter.setCleanJerk2Declaration(weight);
			break;
		case 6:
			lifter.setCleanJerk3Declaration(weight);
			break;
		}
		eventBus.post(new FOPEvent.LiftingOrderUpdated());
	}

//	/**
//	 * @param lifter
//	 * @param lifters1
//	 * @param weight
//	 */
//	private void actualLift(final Athlete lifter, final String weight) {
//		switch (lifter.getAttemptsDone() + 1) {
//		case 1:
//			lifter.setSnatch1ActualLift(weight);
//			break;
//		case 2:
//			lifter.setSnatch2ActualLift(weight);
//			break;
//		case 3:
//			lifter.setSnatch3ActualLift(weight);
//			break;
//		case 4:
//			lifter.setCleanJerk1ActualLift(weight);
//			break;
//		case 5:
//			lifter.setCleanJerk2ActualLift(weight);
//			break;
//		case 6:
//			lifter.setCleanJerk3ActualLift(weight);
//			break;
//		}
//	}

	private void failedLift(EventBus fopBus, Athlete curLifter) {
		logger.debug("calling lifter: {}", curLifter); //$NON-NLS-1$
		fopBus.post(new FOPEvent.AthleteAnnounced());
		fopBus.post(new FOPEvent.DownSignal());
		fopBus.post(new FOPEvent.RefereeDecision(false));
		logger.debug("failed lift for {}", curLifter); //$NON-NLS-1$
		fopBus.post(new FOPEvent.DecisionReset());
	}

	private void successfulLift(EventBus fopBus, Athlete curLifter) {
		logger.debug("calling lifter: {}", curLifter); //$NON-NLS-1$
		fopBus.post(new FOPEvent.AthleteAnnounced());
		fopBus.post(new FOPEvent.DownSignal());
		fopBus.post(new FOPEvent.RefereeDecision(true));
		logger.debug("successful lift for {}", curLifter); //$NON-NLS-1$
		fopBus.post(new FOPEvent.DecisionReset());
	}

}