package com.fploptimizer.fplai.controller;

import com.fploptimizer.fplai.model.Event;
import com.fploptimizer.fplai.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling requests related to events.
 * Provides an endpoint to retrieve the current event.
 */
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * Retrieves the current event.
     *
     * @return a ResponseEntity containing the current event
     */
    @GetMapping("/current")
    public Event getCurrentEvent() {
        return eventService.getCurrentEvent();
    }
}
