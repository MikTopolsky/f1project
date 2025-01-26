package com.example.f1project.controller;

import com.example.f1project.model.Driver;
import com.example.f1project.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/drivers")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @GetMapping
    public String listDrivers(
            @RequestParam(required = false) String round,
            @RequestParam(required = false, defaultValue = "position") String sortBy,
            @RequestParam(required = false, defaultValue = "2024") String season,
            Model model
    ) {
        try {
            driverService.setCurrentSeason(season);

            if (round == null || round.isEmpty()) {
                round = driverService.fetchLastRound();
            }

            List<Driver> drivers = driverService.fetchDriversFromApi(round);

            String raceName = drivers.isEmpty() ? "Unknown Race" : drivers.get(0).getRaceName();

            switch (sortBy) {
                case "team":
                    drivers.sort(Comparator.comparing(Driver::getTeam));
                    break;
                case "position":
                default:
                    drivers.sort(Comparator.comparingInt(Driver::getPosition));
                    break;
            }

            model.addAttribute("drivers", drivers);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("round", round);
            model.addAttribute("season", season);
            model.addAttribute("raceName", raceName);

        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Wystąpił nieoczekiwany błąd: " + e.getMessage());
            return "error";
        }

        return "drivers";
    }
}