package br.ifsp.bes.circuitBreaker.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.CompletableFuture;

@Service
public class WeatherService {

    private final RestTemplate restTemplate;
    private final JedisPool JedisPool;
    private int Timeout = 1000;

    @Value("${weather.api.key}")
    private String apiKey;

    public WeatherService(RestTemplate restTemplate, JedisPool jedisPool) {
        this.restTemplate = restTemplate;
        this.JedisPool = jedisPool;
    }

    @CircuitBreaker(name = "weatherApi", fallbackMethod = "fallbackWeather")
    public CompletableFuture<String> getWeather(String city) {
        return CompletableFuture.supplyAsync(() -> {
            String url = String.format("https://api.weatherapi.com/v1/current.json?key=%s&q=%s&aqi=no", apiKey, city);

            try (Jedis jedis = JedisPool.getResource()) {
                var response = restTemplate.getForObject(url, String.class);
                jedis.setex(city,300, response);
                return response;
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }

        });

    }
    @TimeLimiter(name = "weatherApiTimeout")
    @CircuitBreaker(name = "weatherApiTimeout", fallbackMethod = "fallbackWeather")
    public CompletableFuture<String> getWeatherTimeout(String city) {
        return CompletableFuture.supplyAsync(()->{
            String cached;
            try(Jedis  jedis = JedisPool.getResource()) {
                String url = String.format("https://api.weatherapi.com/v1/current.json?key=%s&q=%s&aqi=no", apiKey, city);
                Thread.sleep(Timeout);
                Timeout = Timeout * 2;
                String response = restTemplate.getForObject(url, String.class);
                jedis.setex("city",300,response);
                return response;
            }catch(Exception e){
                throw new RuntimeException(e.getMessage());
            }
        });
    }

    public CompletableFuture<String> fallbackWeather(String city, Throwable t)
    {
        Timeout= 1000;
        return CompletableFuture.supplyAsync(() -> {
            String cached;
            try (Jedis jedis = JedisPool.getResource()) {
                cached = jedis.get(city);
                return  "Motivo da chamada do fallback: " + t.getMessage() +"\nresultado do cache no fallback: \n" + cached;
            } catch (Exception e) {
                return "Não foi possível recuperar o cache: \n" + e.getMessage() ;
            }

        });

    }
}

