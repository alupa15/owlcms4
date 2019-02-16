package org.ledocte.owlcms.tests;

import org.ledocte.owlcms.state.ICountdownTimer;
import org.ledocte.owlcms.utils.LoggerUtils;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class MockCountdownTimer implements ICountdownTimer {

	final private static Logger logger = (Logger) LoggerFactory.getLogger(MockCountdownTimer.class);

	private int timeRemaining;
	
	public MockCountdownTimer() {
		logger.setLevel(Level.INFO);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ledocte.owlcms.tests.ICountDownTimer#start()
	 */
	@Override
	public void start() {
		logger.debug("starting Time -- timeRemaining = {} \t[{}]",timeRemaining, LoggerUtils.whereFrom());
		timeRemaining = (getTimeRemaining() - 1000);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ledocte.owlcms.tests.ICountDownTimer#stop()
	 */
	@Override
	public void stop() {
		logger.debug("stopping Time -- timeRemaining = {} \t[{}]",timeRemaining, LoggerUtils.whereFrom());
		timeRemaining = (getTimeRemaining());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ledocte.owlcms.tests.ICountDownTimer#getTimeRemaining()
	 */
	@Override
	public int getTimeRemaining() {
		return timeRemaining;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ledocte.owlcms.tests.ICountDownTimer#setTimeRemaining(int)
	 */
	@Override
	public void setTimeRemaining(int timeRemaining) {
		logger.debug("setting Time -- timeRemaining = {}\t[{}]", timeRemaining, LoggerUtils.whereFrom());
		this.timeRemaining = timeRemaining;
	}

}