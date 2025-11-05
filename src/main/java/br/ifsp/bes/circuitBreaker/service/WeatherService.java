package br.ifsp.bes.circuitBreaker.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.sql.Time;
import java.util.concurrent.CompletableFuture;

@Service
public class WeatherService {

    private final RestTemplate restTemplate;
    private int Timeout = 1000;
    private final JedisPool JedisPool;

    @Value("${weather.api.key}")
    private String apiKey;

    public WeatherService(RestTemplate restTemplate, JedisPool jedisPool) {
        this.restTemplate = restTemplate;
        this.JedisPool = jedisPool;
    }

    @TimeLimiter(name = "weatherApi", fallbackMethod = "fallbackWeather")
    @CircuitBreaker(name = "weatherApi")
    public CompletableFuture<String> getWeather(String city) {
        return CompletableFuture.supplyAsync(() -> {
            String url = String.format("https://api.weatherapi.com/v1/current.json?key=%s&q=%s&aqi=no", apiKey, city);

            try (Jedis jedis = JedisPool.getResource()) {
                Thread.sleep(Timeout);
                Timeout = Timeout * 2;
                var response = restTemplate.getForObject(url, String.class);
                jedis.set(city, response);
                return response;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        });

    }

    public CompletableFuture<String> fallbackWeather(String city) {
        return CompletableFuture.supplyAsync(() -> {
            String cached;
            try (Jedis jedis = JedisPool.getResource()) {
                cached = jedis.get(city);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return cached;
        });

    }
}

