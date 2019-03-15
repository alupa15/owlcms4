/***
 * Copyright (c) 2018-2019 Jean-François Lamy
 * 
 * This software is licensed under the the Apache 2.0 License amended with the
 * Commons Clause.
 * License text at https://github.com/jflamy/owlcms4/master/License
 * See https://redislabs.com/wp-content/uploads/2018/10/Commons-Clause-White-Paper.pdf
 */
package app.owlcms.tests;

import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.vaadin.flow.component.UI;

import app.owlcms.state.ICountdownTimer;
import app.owlcms.state.UIEvent;
import app.owlcms.utils.LoggerUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class MockCountdownTimer implements ICountdownTimer {

	final private static Logger logger = (Logger) LoggerFactory.getLogger(MockCountdownTimer.class);

	private int timeRemaining;
	
	public MockCountdownTimer() {
		logger.setLevel(Level.INFO);
	}

	/***
	 * Remote control commands, via events
	 */
	
	@Override
	@Subscribe
	public void startTimer(UIEvent.StartTime e) {
		Integer milliseconds = e.getTimeRemaining();
		setTimeRemaining(milliseconds);
		start();
	}
	
	@Override
	@Subscribe
	public void stopTimer(UIEvent.StopTime e) {
		stop();
	}
	
	@Override
	@Subscribe
	public void setTimer(UIEvent.SetTime e) {
		Integer milliseconds = e.getTimeRemaining();
		setTimeRemaining(milliseconds);
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see app.owlcms.tests.ICountDownTimer#start()
	 */
	@Override
	public void start() {
		logger.debug("starting Time -- timeRemaining = {} \t[{}]",timeRemaining, LoggerUtils.whereFrom());
		timeRemaining = (getTimeRemaining() - 1000);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see app.owlcms.tests.ICountDownTimer#stop()
	 */
	@Override
	public void stop() {
		logger.debug("stopping Time -- timeRemaining = {} \t[{}]",timeRemaining, LoggerUtils.whereFrom());
		timeRemaining = (getTimeRemaining());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see app.owlcms.tests.ICountDownTimer#getTimeRemaining()
	 */
	@Override
	public int getTimeRemaining() {
		return timeRemaining;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see app.owlcms.tests.ICountDownTimer#setTimeRemaining(int)
	 */
	@Override
	public void setTimeRemaining(int timeRemaining) {
		logger.debug("setting Time -- timeRemaining = {}\t[{}]", timeRemaining, LoggerUtils.whereFrom());
		this.timeRemaining = timeRemaining;
	}

	@Override
	public void timeOver(UI originatingUI) {
		stop();
		timeRemaining = 0;
	}

}