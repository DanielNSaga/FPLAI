package com.fploptimizer.fplai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fploptimizer.fplai.exception.ResourceNotFoundException;
import com.fploptimizer.fplai.model.Event;
import com.fploptimizer.fplai.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Iterator;

/**
 * Service implementation for handling event-related operations.
 * This service interacts with the Fantasy Premier League API to fetch the current event details
 * and stores the relevant information in the repository.
 */
@Service
public class EventServiceImpl implements EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventServiceImpl.class);
    private static final String API_URL = "https://fantasy.premierleague.com/api/bootstrap-static/";

    private final RestTemplate restTemplate;
    private final EventRepository eventRepository;

    /**
     * Constructor-based dependency injection.
     *
     * @param restTemplate    the RestTemplate to be used for making HTTP requests.
     * @param eventRepository the repository used for storing event data.
     */
    public EventServiceImpl(RestTemplate restTemplate, EventRepository eventRepository) {
        this.restTemplate = restTemplate;
        this.eventRepository = eventRepository;
    }

    /**
     * Fetches the current event from the Fantasy Premier League API.
     * The method parses the JSON response to extract details about the current event
     * (such as season, gameweek, and total players) and stores this information
     * in the event repository.
     */
    @Override
    public void fetchCurrentEvent() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response;
        try {
            // Make the HTTP GET request to the FPL API
            response = restTemplate.exchange(API_URL, HttpMethod.GET, entity, String.class);
        } catch (Exception e) {
            logger.error("Failed to fetch data from the FPL API", e);
            return;
        }

        String jsonResponse = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Parse the JSON response
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            JsonNode eventsArray = rootNode.get("events");
            if (eventsArray == null || !eventsArray.isArray()) {
                logger.warn("Events array is missing or not an array in the JSON response.");
                return;
            }

            int totalPlayers = rootNode.get("total_players") != null ? rootNode.get("total_players").asInt() : 0;

            // Iterate through the events array to find the current event
            Iterator<JsonNode> elements = eventsArray.elements();
            while (elements.hasNext()) {
                JsonNode eventNode = elements.next();
                boolean isFinished = eventNode.get("finished") != null && eventNode.get("finished").asBoolean();

                // Log each event to see finished status
                logger.debug("Gameweek ID: {}, Finished: {}", eventNode.get("id").asInt(), isFinished);

                // If an event is not finished, it's the current event
                if (!isFinished) {
                    int gameweek = eventNode.get("id") != null ? eventNode.get("id").asInt() : 0;
                    String id = "GW" + gameweek;  // Create an ID based on the gameweek

                    Event currentEvent = new Event(id, null, gameweek, totalPlayers);
                    eventRepository.save(currentEvent);
                    logger.debug("Current event found and saved: {}", currentEvent);
                    break;
                }
            }
        } catch (IOException e) {
            logger.error("Failed to parse the JSON response from the FPL API", e);
        } catch (Exception e) {
            logger.error("An unexpected error occurred while processing the FPL API response", e);
        }
    }

    /**
     * Retrieves the most recent event stored in the repository.
     *
     * <p>This method fetches the event with the highest gameweek, which represents the current or most recent event.
     * If no event is found, it throws a ResourceNotFoundException.</p>
     *
     * @return the current or most recent Event
     * @throws ResourceNotFoundException if no current event is found in the repository
     */
    @Override
    public Event getCurrentEvent() {
        return (Event) eventRepository.findTopByOrderByGameweekDesc()
                .orElseThrow(() -> new ResourceNotFoundException("No current event found"));
    }
}
