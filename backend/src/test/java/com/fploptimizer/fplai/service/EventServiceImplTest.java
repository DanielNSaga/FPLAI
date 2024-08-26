package com.fploptimizer.fplai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fploptimizer.fplai.model.Event;
import com.fploptimizer.fplai.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class EventServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventServiceImpl eventService;

    @Mock
    private ObjectMapper objectMapper;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void fetchCurrentEvent_returnsEvent_whenCurrentEventExists() throws Exception {
        // Arrange
        String jsonResponse = "{ \"events\": [ { \"id\": 1, \"finished\": true }, { \"id\": 2, \"finished\": false } ]," +
                "\"game_settings\": { \"season\": \"2023/24\" }, \"total_players\": 1000000 }";
        ResponseEntity<String> responseEntity = ResponseEntity.ok(jsonResponse);

        when(restTemplate.exchange(eq("https://fantasy.premierleague.com/api/bootstrap-static/"), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(String.class))).thenReturn(responseEntity);

        // Act
        Event currentEvent = eventService.getCurrentEvent();

        // Assert
        assertNotNull(currentEvent);
        assertEquals("2", currentEvent.getId());
        assertEquals("2023/24", currentEvent.getSeason());
        assertEquals(2, currentEvent.getGameweek());
        assertEquals(1000000, currentEvent.getTotalPlayers());
        verify(eventRepository, times(1)).save(currentEvent);
    }

    @Test
    void fetchCurrentEvent_returnsNull_whenNoCurrentEventExists() throws Exception {
        // Arrange
        String jsonResponse = "{ \"events\": [ { \"id\": 1, \"finished\": true }, { \"id\": 2, \"finished\": true } ]," +
                "\"game_settings\": { \"season\": \"2023/24\" }, \"total_players\": 1000000 }";
        ResponseEntity<String> responseEntity = ResponseEntity.ok(jsonResponse);

        when(restTemplate.exchange(eq("https://fantasy.premierleague.com/api/bootstrap-static/"), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(String.class))).thenReturn(responseEntity);

        // Act
        Event currentEvent = eventService.getCurrentEvent();

        // Assert
        assertNull(currentEvent);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void fetchCurrentEvent_handlesIOExceptionGracefully() throws Exception {
        // Arrange
        String jsonResponse = "{ \"events\": [ { \"id\": 1, \"finished\": true } ]," +
                "\"game_settings\": { \"season\": \"2023/24\" }, \"total_players\": 1000000 }";
        ResponseEntity<String> responseEntity = ResponseEntity.ok(jsonResponse);

        when(restTemplate.exchange(eq("https://fantasy.premierleague.com/api/bootstrap-static/"), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(String.class))).thenReturn(responseEntity);

        // Use doThrow to simulate an IOException in readTree method
        doThrow(new RuntimeException(new IOException())).when(objectMapper).readTree(any(String.class));

        // Act
        Event currentEvent = eventService.getCurrentEvent();

        // Assert
        assertNull(currentEvent);
        verify(eventRepository, never()).save(any(Event.class));
    }
}
