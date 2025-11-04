package br.ifsp.bes.circuitBreaker.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

@Service
public class WeatherService {

    private final RestTemplate restTemplate;

    @Value("${weather.api.key}")
    private String apiKey;

    public WeatherService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // ============================================================
    // Cenário 1 — Requisição válida para a WeatherAPI
    // ============================================================
    @CircuitBreaker(name = "weatherApi", fallbackMethod = "fallbackWeather")
    public String getWeather(String city) {
        String url = String.format("https://api.weatherapi.com/v1/current.json?key=%s&q=%s&aqi=no", apiKey, city);
        return restTemplate.getForObject(url, String.class);
    }

    // ============================================================
    // Cenário 2 — Erro de Endpoint
    // ============================================================
    @CircuitBreaker(name = "weatherApi", fallbackMethod = "fallbackWeather")
    public String getWeatherInvalidEndpoint(String city) {
        String url = String.format("https://api.weatherapi.com/v1/invalid.json?key=%s&q=%s", apiKey, city);
        return restTemplate.getForObject(url, String.class);
    }

    // ============================================================
    // Cenário 3 — Timeout Simulado
    // ============================================================
    @CircuitBreaker(name = "weatherApi", fallbackMethod = "fallbackWeather")
    public String getWeatherWithDelay(String city) {
        try {
            // Simula lentidão no serviço remoto
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String url = String.format("https://api.weatherapi.com/v1/current.json?key=%s&q=%s&aqi=no", apiKey, city);
        return restTemplate.getForObject(url, String.class);
    }

    // ============================================================
    // Cenário 4 — Chave de API Inválida
    // ============================================================
    @CircuitBreaker(name = "weatherApi", fallbackMethod = "fallbackWeather")
    public String getWeatherInvalidApiKey(String city) {
        String invalidKey = "INVALID_KEY_123";
        String url = String.format(
                "https://api.weatherapi.com/v1/current.json?key=%s&q=%s&aqi=no",
                invalidKey, city
        );
        return restTemplate.getForObject(url, String.class);
    }

    // ============================================================
    // Metodo fallback genérico para todos os casos
    // ============================================================
    public String fallbackWeather(String city, Throwable t) {
        return String.format("Circuit Breaker ON. Não foi possível obter o clima de '%s': %s", city, t.getMessage());
    }
}

