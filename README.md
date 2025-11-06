# Circuit Breaker Tutorial com Resilience4j + Redis + Spring Boot

Este projeto demonstra o uso do **Circuit Breaker** e **TimeLimiter** usando a biblioteca **Resilience4j**, com cache em **Redis** para fallback automático.

A aplicação consome a API pública do [WeatherAPI](https://www.weatherapi.com/), armazenando o resultado no Redis e simulando falhas controladas para demonstrar a resiliência do sistema.

---

## Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 3**
- **Resilience4j**
- **Docker Compose**
- **Maven**
- **Redis**
- **Jedis** (cliente Redis)
- **WeatherAPI**

## 🚀 Como Rodar o Projeto

### 1️⃣ Pré-requisitos

- **Docker** e **Docker Compose** instalados
- Conta gratuita no [WeatherAPI](https://www.weatherapi.com/) para obter sua **API key**

---

### 2️⃣ Configurar a variável de ambiente
No terminal, defina a variável `WEATHER_API_KEY` (substitua `<SUA_CHAVE>` pela sua API Key real):

#### 🪟 Windows (PowerShell)
```powershell
$env:WEATHER_API_KEY="<SUA_CHAVE>"
````

#### 🐧 Linux / macOS
````bash
export WEATHER_API_KEY="<SUA_CHAVE>"
````

### 3️⃣ Subir os containers

Na raiz do projeto, execute:
````
docker compose up --build
````

### 4️⃣ Verificar se está rodando

A API estará disponível em:
````
http://localhost:8080
````

## ☁️ Endpoints Disponíveis
✅ Consultar clima - Retorna o JSON da WeatherAPI e salva no Redis por 5 minutos (300s).
````
GET /weather/ok/{city}
````

Exemplo:
````
http://localhost:8080/weather/ok/sao-paulo
````

