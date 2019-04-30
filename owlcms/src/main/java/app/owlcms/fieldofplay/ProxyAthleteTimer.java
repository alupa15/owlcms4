/***
 * Copyright (c) 2009-2019 Jean-François Lamy
 * 
 * Licensed under the Non-Profit Open Software License version 3.0  ("Non-Profit OSL" 3.0)  
 * License text at https://github.com/jflamy/owlcms4/blob/master/LICENSE.txt
 */
package app.owlcms.fieldofplay;

import org.slf4j.LoggerFactory;

import app.owlcms.utils.LoggerUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Class ProxyBreakTimer. Relay timer instructions from {@link FieldOfPlay} to the actual timers
 * associated with each screen. Memorize the elapsed time and timer state.
 *
 * @author Jean-François Lamy
 */
public class ProxyAthleteTimer implements IProxyTimer {

	final private Logger logger = (Logger) LoggerFactory.getLogger(ProxyAthleteTimer.class);
	{
		logger.setLevel(Level.INFO);
	}

	private int timeRemaining;
	private FieldOfPlay fop;
	private long startMillis;
	private long stopMillis;

	/**
	 * Instantiates a new countdown timer.
	 * 
	 * @param fop
	 */
	public ProxyAthleteTimer(FieldOfPlay fop) {
		this.fop = fop;
	}

	/* (non-Javadoc)
	 * @see app.owlcms.tests.ICountDownTimer#getTimeRemaining() */
	@Override
	public int getTimeRemaining() {
		return timeRemaining;
	}

	/* (non-Javadoc)
	 * @see app.owlcms.fieldofplay.IProxyTimer#setTimeRemaining(int) */
	@Override
	public void setTimeRemaining(int timeRemaining) {
		logger.debug("setting Time -- timeRemaining = {} [{}]", timeRemaining, LoggerUtils.whereFrom());
		this.timeRemaining = timeRemaining;
		fop.getUiEventBus().post(new UIEvent.SetTime(timeRemaining, null));
	}

	/* (non-Javadoc)
	 * @see app.owlcms.tests.ICountDownTimer#start() */
	@Override
	public void start() {
		startMillis = System.currentTimeMillis();
		logger.debug("starting Time -- timeRemaining = {} [{}]", timeRemaining, LoggerUtils.whereFrom());
		fop.getUiEventBus().post(new UIEvent.StartTime(timeRemaining, null));
	}

	/* (non-Javadoc)
	 * @see app.owlcms.tests.ICountDownTimer#stop() */
	@Override
	public void stop() {
		stopMillis = System.currentTimeMillis();
		long elapsed = stopMillis - startMillis;
		timeRemaining = (int) (timeRemaining - elapsed);
		logger.debug("stopping Time -- timeRemaining = {} [{}]", timeRemaining, LoggerUtils.whereFrom());
		fop.getUiEventBus().post(new UIEvent.StopTime(timeRemaining, null));
	}

	@Override
	public void timeOut(Object origin) {
		this.stop();
		fop.getFopEventBus().post(new FOPEvent.TimeOver(origin));
	}

}