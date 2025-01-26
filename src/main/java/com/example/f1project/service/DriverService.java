package com.example.f1project.service;

import com.example.f1project.model.Driver;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class DriverService {
    private final RestTemplate restTemplate;
    private final List<Driver> driversCache = new ArrayList<>();
    private String raceName = null;
    private String currentSeason = "2024";

    public DriverService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getCurrentSeason() {
        return currentSeason;
    }

    public void setCurrentSeason(String season) {
        this.currentSeason = season;
    }

    public List<Driver> fetchDriversFromApi(String round) {
        String apiUrl = "https://ergast.com/api/f1/" + currentSeason + "/" + round + "/results.json";
        try {
            Map<String, Object> response = restTemplate.getForObject(apiUrl, Map.class);

            Map<String, Object> mrData = (Map<String, Object>) response.get("MRData");
            Map<String, Object> raceTable = (Map<String, Object>) mrData.get("RaceTable");
            List<Map<String, Object>> races = (List<Map<String, Object>>) raceTable.get("Races");

            if (races == null || races.isEmpty()) {
                throw new IllegalArgumentException("Nie znaleziono danych dla sezonu " + currentSeason + " i rundy " + round);
            }

            Map<String, Object> race = races.get(0);
            raceName = (String) race.get("raceName");
            List<Map<String, Object>> results = (List<Map<String, Object>>) race.get("Results");

            driversCache.clear();
            for (Map<String, Object> result : results) {
                Map<String, Object> driverData = (Map<String, Object>) result.get("Driver");
                Map<String, Object> constructorData = (Map<String, Object>) result.get("Constructor");

                Driver driver = new Driver();
                driver.setFirstName((String) driverData.get("givenName"));
                driver.setLastName((String) driverData.get("familyName"));
                driver.setTeam((String) constructorData.get("name"));
                driver.setPoints(Integer.parseInt((String) result.get("points")));
                driver.setPosition(Integer.parseInt((String) result.get("position")));
                driver.setRaceName(raceName);

                driversCache.add(driver);
            }

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Wystąpił problem z pobraniem danych z API: " + e.getMessage());
        }

        return driversCache;
    }

    public String fetchLastRound() {
        String apiUrl = "https://ergast.com/api/f1/" + currentSeason + ".json";
        try {
            Map<String, Object> response = restTemplate.getForObject(apiUrl, Map.class);
            Map<String, Object> mrData = (Map<String, Object>) response.get("MRData");
            Map<String, Object> raceTable = (Map<String, Object>) mrData.get("RaceTable");
            List<Map<String, Object>> races = (List<Map<String, Object>>) raceTable.get("Races");

            if (!races.isEmpty()) {
                Map<String, Object> lastRace = races.get(races.size() - 1);
                return (String) lastRace.get("round");
            }
        } catch (Exception e) {
            System.err.println("Error fetching last round: " + e.getMessage());
        }
        return "1";
    }
}