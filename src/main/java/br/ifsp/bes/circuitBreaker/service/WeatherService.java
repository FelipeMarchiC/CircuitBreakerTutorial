package br.ifsp.bes.circuitBreaker.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.CompletableFuture;

@Service
public class WeatherService {

    private final RestTemplate restTemplate;
    private int Timeout = 6000;

    @Value("${weather.api.key}")
    private String apiKey;

    public WeatherService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    @TimeLimiter(name = "weatherApi", fallbackMethod = "fallbackWeather")
    @CircuitBreaker(name = "weatherApi", fallbackMethod = "fallbackWeather")
    public CompletableFuture<String> getWeather(String city) {
        return CompletableFuture.supplyAsync(()->{
            String url = String.format("https://api.weatherapi.com/v1/current.json?key=%s&q=%s&aqi=no", apiKey, city);
            try{
                Thread.sleep(Timeout);

            }catch(InterruptedException e){
                throw new IllegalStateException("estourou o timeout");

            }
            return restTemplate.getForObject(url, String.class);
        });

    }

    public CompletableFuture<String> fallbackWeather(String city, Throwable t) {
        return CompletableFuture.completedFuture("testando o fallback");
    }
}

