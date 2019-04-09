package app.owlcms.ui.shared;

import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;

import app.owlcms.state.FieldOfPlayState;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

// @formatter:off
public interface SafeEventBusRegistration {
	
	Logger logger = (Logger) LoggerFactory.getLogger(SafeEventBusRegistration.class);


	public default EventBus uiEventBusRegister(Component c, FieldOfPlayState fop) {
		
		{logger.setLevel(Level.INFO);}
		
		UI ui = c.getUI().get();
		EventBus uiEventBus = fop.getUiEventBus();
		uiEventBus.register(c);
		ui.addBeforeLeaveListener((e) -> {
			logger.debug("leaving : unregister {} from {}", e.getSource(), uiEventBus.identifier());
			try {uiEventBus.unregister(c);} catch (Exception ex) {}
		});
		ui.addDetachListener((e) -> {
			logger.debug("detaching: unregister {} from {}", e.getSource(), uiEventBus.identifier());
			try {uiEventBus.unregister(c);} catch (Exception ex) {}
		});
		return uiEventBus;
	}
	
	public default EventBus fopEventBusRegister(Component c, FieldOfPlayState fop) {
		
		{logger.setLevel(Level.INFO);}
		
		UI ui = c.getUI().get();
		EventBus fopEventBus = fop.getEventBus();
		fopEventBus.register(c);
		ui.addBeforeLeaveListener((e) -> {
			logger.debug("leaving : unregister {} from {}", e.getSource(), fopEventBus.identifier());
			try {fopEventBus.unregister(c);} catch (Exception ex) {}
		});
		ui.addDetachListener((e) -> {
			logger.debug("detaching: unregister {} from {}", e.getSource(), fopEventBus.identifier());
			try {fopEventBus.unregister(c);} catch (Exception ex) {}
		});
		return fopEventBus;
	}

}