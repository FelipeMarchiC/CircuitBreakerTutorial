package br.ifsp.bes.circuitBreaker.controller;

import br.ifsp.bes.circuitBreaker.service.WeatherService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/ok/{city}")
    public String normal(@PathVariable String city) {
        return weatherService.getWeather(city);
    }

}
