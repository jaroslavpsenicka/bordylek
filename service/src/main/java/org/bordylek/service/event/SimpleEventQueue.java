package org.bordylek.service.event;

import org.bordylek.service.annotation.Subscribe;
import org.bordylek.service.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Primary
public class SimpleEventQueue implements EventQueue {

	private Map<String, List<EventHandler>> handlers;

	private static final Logger LOG = LoggerFactory.getLogger(SimpleEventQueue.class);

	@Autowired
	public void setCtx(ApplicationContext ctx) throws Exception {
		handlers = new HashMap<String, List<EventHandler>>();
		Map<String, Object> beans = ctx.getBeansWithAnnotation(Subscribe.class);
		LOG.info("Registering event handlers:");				
		for (Object bean : beans.values()) {
			if (bean instanceof EventHandler) {
				Subscribe annotation = (bean instanceof Advised) ?
					((Advised) bean).getTargetSource().getTarget().getClass().getAnnotation(Subscribe.class) :
					bean.getClass().getAnnotation(Subscribe.class);
				String eventKey = annotation.domain()+"."+annotation.name();
				List<EventHandler> handlersList = handlers.get(eventKey);
				if (handlersList == null) {
					handlersList = new ArrayList<EventHandler>();
					handlers.put(eventKey, handlersList);
				}
				LOG.info(" - "+bean.getClass().getName());
				handlersList.add((EventHandler)bean);
			}
		}
	}

	@Async
	public void send(Event e) {
		LOG.debug("Event "+e.getDomainName()+"."+e.getEventName());
		List<EventHandler> hlist = getHandlers(e.getDomainName(), e.getEventName());
		if (hlist != null) for (EventHandler h : hlist) try {
			LOG.debug("- handler "+h);
			h.handleEvent(e.getDomainName(), e.getDomainName(), e.getParameters());
		} catch (Exception ex) {
			LOG.error("Error processing event "+e+" using handler "+h, ex);
		}
	}

	protected List<EventHandler> getHandlers(String domainName, String eventName) {
		return handlers.get(domainName+"."+eventName);
	}

}
