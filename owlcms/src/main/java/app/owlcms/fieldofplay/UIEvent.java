/***
 * Copyright (c) 2009-2019 Jean-François Lamy
 *
 * Licensed under the Non-Profit Open Software License version 3.0  ("Non-Profit OSL" 3.0)
 * License text at https://github.com/jflamy/owlcms4/blob/master/LICENSE.txt
 */
package app.owlcms.fieldofplay;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.vaadin.flow.component.UI;

import app.owlcms.data.athlete.Athlete;
import app.owlcms.data.group.Group;
import app.owlcms.ui.shared.BreakManagement.CountdownType;

/**
 * UIEvents are triggered in response to field of play events (FOPEvents). Each
 * field of play has an associated uiEventBus on which the user interface
 * commands are posted. The various browsers subscribe to UIEvents and react
 * accordingly.
 *
 * @author owlcms
 */
public class UIEvent {

    static public class BarbellOrPlatesChanged extends UIEvent {
        public BarbellOrPlatesChanged(Object object) {
            super(object);
        }
    }

    /**
     * Class BreakDone.
     */
    static public class BreakDone extends UIEvent {

        /**
         * Instantiates a new break done.
         *
         * @param origin the origin
         */
        public BreakDone(Object origin) {
            super(origin);
        }

    }

    /**
     * Class BreakPaused.
     */
    static public class BreakPaused extends UIEvent {

        /**
         * Instantiates a new break paused.
         *
         * @param origin the origin
         */
        public BreakPaused(Object origin) {
            super(origin);
        }

    }

    /**
     * Class BreakSetTime
     */
    static public class BreakSetTime extends UIEvent {

        protected Integer timeRemaining;
        protected boolean indefinite;
        protected LocalDateTime end;
        protected BreakType breakType;
        protected CountdownType countdownType;

        public BreakSetTime(BreakType bt, CountdownType ct, Integer timeRemaining, boolean indefinite, Object origin) {
            super(origin);
            if (bt == BreakType.TECHNICAL || bt == BreakType.JURY) {
                this.indefinite = true;
                this.end = null;
            } else {
                this.timeRemaining = timeRemaining;
                this.indefinite = false;
                this.end = LocalDateTime.now().plusNanos(timeRemaining * 1000);
            }
            this.breakType = bt;
            this.countdownType = ct;
        }

        public BreakSetTime(BreakType bt, CountdownType ct, LocalDateTime end, Object origin) {
            super(origin);
            if (bt == BreakType.TECHNICAL || bt == BreakType.JURY) {
                this.indefinite = true;
                this.end = end;
            } else {
                timeRemaining = (int) LocalDateTime.now().until(end, ChronoUnit.MILLIS);
                this.indefinite = false;
                this.end = end;
            }
            this.breakType = bt;
            this.countdownType = ct;
        }

        public BreakType getBreakType() {
            return breakType;
        }

        public LocalDateTime getEnd() {
            return end;
        }

        public Integer getTimeRemaining() {
            return timeRemaining;
        }

        /**
         * @return true if break lasts indefinitely and timeRemaining should be ignored
         */
        public boolean isIndefinite() {
            return indefinite;
        }
    }

    /**
     * Class BreakStarted.
     */
    // MUST NOT EXTEND otherwise subscription triggers on supertype as well
    static public class BreakStarted extends UIEvent {

        private boolean displayToggle;

        protected Integer timeRemaining;

        protected boolean indefinite;

        protected LocalDateTime end;

        protected BreakType breakType;

        protected CountdownType countdownType;

        public BreakStarted(Integer timeRemaining, Object origin, boolean displayToggle, BreakType bt,
                CountdownType ct) {
            super(origin);
            this.timeRemaining = timeRemaining;
            this.indefinite = (ct != null && ct == CountdownType.TARGET) || (timeRemaining == null);
            if (timeRemaining != null) {
                this.end = LocalDateTime.now().plusNanos(timeRemaining * 1000);
            }
            this.breakType = bt;
            this.countdownType = ct;
            this.setDisplayToggle(displayToggle);
        }

        public BreakType getBreakType() {
            return breakType;
        }

        public LocalDateTime getEnd() {
            return end;
        }

        public Integer getTimeRemaining() {
            return timeRemaining;
        }

        /**
         * @return true if is a request for toggling display (and not an actual break
         *         start)
         */
        public boolean isDisplayToggle() {
            return displayToggle;
        }

        /**
         * @return true if break lasts indefinitely and timeRemaining should be ignored
         */
        public boolean isIndefinite() {
            return indefinite;
        }

        /**
         * @param displayToggle true to request switching to Break Timer
         */
        public void setDisplayToggle(boolean displayToggle) {
            this.displayToggle = displayToggle;
        }

        @Override
        public String toString() {
            return "UIEvent.BreakStarted [displayToggle=" + displayToggle + ", timeRemaining=" + timeRemaining
                    + ", indefinite=" + indefinite + ", end=" + end + ", breakType=" + breakType + ", countdownType="
                    + countdownType + "]";
        }
    }

    /**
     * Class ExplicitDecision.
     */
    static public class Decision extends UIEvent {

        /** decision. */
        public Boolean decision = null;

        /** ref 1. */
        public Boolean ref1;

        /** ref 2. */
        public Boolean ref2;

        /** ref 3. */
        public Boolean ref3;

        /**
         * Instantiates a new referee decision.
         *
         * @param decision the decision
         * @param ref1     the ref 1
         * @param ref2     the ref 2
         * @param ref3     the ref 3
         * @param origin   the origin
         */
        public Decision(Athlete a, Boolean decision, Boolean ref1, Boolean ref2, Boolean ref3, Object origin) {
            super(a, origin);
            this.decision = decision;
            this.ref1 = ref1;
            this.ref2 = ref2;
            this.ref3 = ref3;
        }

    }

    /**
     * Class DecisionReset.
     */
    static public class DecisionReset extends UIEvent {

        /**
         * Instantiates a new decision reset.
         *
         * @param origin the origin
         */
        public DecisionReset(Object origin) {
            super(origin);
        }
    }

    /**
     * Class DownSignal.
     */
    static public class DownSignal extends UIEvent {

        /**
         * Instantiates a new down signal.
         *
         * @param origin the origin
         */
        public DownSignal(Object origin) {
            super(origin);
        }
    }

    static public class GlobalRankingUpdated extends UIEvent {
        public GlobalRankingUpdated(Object object) {
            super(object);
        }
    }

    static public class GroupDone extends UIEvent {

        private Group group;

        /**
         * Instantiates a new athlete announced.
         *
         * @param athlete the athlete
         * @param ui      the ui
         */
        public GroupDone(Group group, UI ui) {
            super(ui);
            this.setGroup(group);
        }

        public Group getGroup() {
            return group;
        }

        public void setGroup(Group group) {
            this.group = group;
        }
    }

    /**
     * Class LiftingOrderUpdated.
     */
    static public class LiftingOrderUpdated extends UIEvent {

        private Athlete nextAthlete;
        private Athlete previousAthlete;
        private Integer timeAllowed;
        private List<Athlete> liftingOrder;
        private List<Athlete> displayOrder;
        private boolean currentDisplayAffected;
        private Athlete changingAthlete;
        private boolean displayToggle;
        private boolean inBreak;

        /**
         * Instantiates a new lifting order updated command.
         *
         * @param athlete         the current athlete after recalculation
         * @param nextAthlete     the next athlete that will lift (cannot be the same as
         *                        athlete)
         * @param previousAthlete the last athlete to have lifted (can be the same as
         *                        athlete)
         * @param changingAthlete the athlete who triggered the lifting update
         * @param liftingOrder    the lifting order
         * @param displayOrder    the display order
         * @param timeAllowed     the time allowed
         * @param displayToggle   if true, just update display according to lifting
         *                        order.
         * @param origin          the origin
         */
        public LiftingOrderUpdated(Athlete athlete, Athlete nextAthlete, Athlete previousAthlete,
                Athlete changingAthlete, List<Athlete> liftingOrder, List<Athlete> displayOrder, Integer timeAllowed,
                boolean currentDisplayAffected, boolean displayToggle, Object origin, boolean inBreak) {
            super(athlete, origin);
            this.nextAthlete = nextAthlete;
            this.previousAthlete = previousAthlete;
            this.changingAthlete = changingAthlete;
            this.timeAllowed = timeAllowed;
            this.liftingOrder = liftingOrder;
            this.displayOrder = displayOrder;
            this.currentDisplayAffected = currentDisplayAffected;
            this.setDisplayToggle(displayToggle);
            this.setInBreak(inBreak);
        }

        public Athlete getChangingAthlete() {
            return changingAthlete;
        }

        /**
         * Gets the display order.
         *
         * @return the display order
         */
        public List<Athlete> getDisplayOrder() {
            return displayOrder;
        }

        /**
         * Gets the lifting order.
         *
         * @return the lifting order
         */
        public List<Athlete> getLiftingOrder() {
            return liftingOrder;
        }

        /**
         * Gets the next athlete.
         *
         * @return the next athlete
         */
        public Athlete getNextAthlete() {
            return nextAthlete;
        }

        /**
         * Gets the previous athlete.
         *
         * @return the previous athlete
         */
        public Athlete getPreviousAthlete() {
            return previousAthlete;
        }

        /**
         * Gets the time allowed.
         *
         * @return the timeAllowed
         */
        public Integer getTimeAllowed() {
            return timeAllowed;
        }

        /**
         * @return true if the current event requires to stop the timer
         */
        public boolean isCurrentDisplayAffected() {
            return currentDisplayAffected;
        }

        public boolean isDisplayToggle() {
            return displayToggle;
        }

        public boolean isInBreak() {
            return inBreak;
        }

        public void setDisplayToggle(boolean displayToggle) {
            this.displayToggle = displayToggle;
        }

        public void setInBreak(boolean inBreak) {
            this.inBreak = inBreak;
        }

    }

    /**
     * Individual referee decision.
     *
     * No subclassing wrt ExplicitDecision because @Subscribe must be distinct.
     *
     * @author owlcms
     */
    static public class RefereeUpdate extends UIEvent {
        public Boolean ref1;
        public Boolean ref2;
        public Boolean ref3;
        public Integer ref1Time;
        public Integer ref2Time;
        public Integer ref3Time;

        public RefereeUpdate(Athlete a, Boolean ref1, Boolean ref2, Boolean ref3, Integer refereeTime,
                Integer refereeTime2, Integer refereeTime3, Object origin) {
            super(a, origin);
            this.ref1 = ref1;
            this.ref2 = ref2;
            this.ref3 = ref3;
            this.ref1Time = refereeTime;
            this.ref2Time = refereeTime2;
            this.ref3Time = refereeTime3;
        }
    }

    /**
     * Class SetTime.
     */
    static public class SetTime extends UIEvent {

        private Integer timeRemaining;

        /**
         * Instantiates a new sets the time.
         *
         * @param timeRemaining the time remaining
         * @param origin        the origin
         */
        public SetTime(Integer timeRemaining, Object origin) {
            super(origin);
            this.timeRemaining = timeRemaining;
        }

        /**
         * Gets the time remaining.
         *
         * @return the time remaining
         */
        public Integer getTimeRemaining() {
            return timeRemaining;
        }

    }

    public static class StartLifting extends UIEvent {
        private Group group;

        public StartLifting(Group group, Object object) {
            super(object);
            this.setGroup(group);
        }

        public Group getGroup() {
            return group;
        }

        public void setGroup(Group group) {
            this.group = group;
        }
    }

    /**
     * Class StartTime.
     */
    static public class StartTime extends UIEvent {

        private Integer timeRemaining;
        private boolean silent;

        /**
         * Instantiates a new start time.
         *
         * @param timeRemaining the time remaining
         * @param origin        the origin
         * @param silent
         */
        public StartTime(Integer timeRemaining, Object origin, boolean silent) {
            super(origin);
            this.timeRemaining = timeRemaining;
            this.silent = silent;
        }

        /**
         * Gets the time remaining.
         *
         * @return the time remaining
         */
        public Integer getTimeRemaining() {
            return timeRemaining;
        }

        public boolean isSilent() {
            return silent;
        }

    }

    /**
     * Class StopTime.
     */
    static public class StopTime extends UIEvent {

        private int timeRemaining;

        /**
         * Instantiates a new stop time.
         *
         * @param timeRemaining the time remaining
         * @param origin        the origin
         */
        public StopTime(int timeRemaining, Object origin) {
            super(origin);
            this.timeRemaining = timeRemaining;
        }

        /**
         * Gets the time remaining.
         *
         * @return the time remaining
         */
        public Integer getTimeRemaining() {
            return timeRemaining;
        }
    }

    public static class SwitchGroup extends UIEvent {
        private Group group;

        public SwitchGroup(Group group, Object object) {
            super(object);
            this.setGroup(group);
        }

        public Group getGroup() {
            return group;
        }

        public void setGroup(Group group) {
            this.group = group;
        }
    }

    private Athlete athlete;

    private Object origin;

    private UIEvent(Athlete athlete, Object origin) {
        this(origin);
        this.athlete = athlete;
    }

    private UIEvent(Object origin) {
        this.origin = origin;
    }

    /**
     * Gets the athlete.
     *
     * @return the athlete
     */
    public Athlete getAthlete() {
        return athlete;
    }

    /**
     * Gets the origin.
     *
     * @return the originating object
     */
    public Object getOrigin() {
        return origin;
    }

}
