# java-favorite

Esta aplicação java cria instrumentação manual do opentelemetry.

# Para Usar:

## Requisitos
* Java 17+
* Docker 16+


## 1 - Rode o Redis: 
Na pasta "redis-cluster" execute o comando:

```shell script
docker-compose -f redis-docker-compose.yml up -d
```
## 2 - Rode o colletor
Na pasta "colletor-otlp" execute o comando:

```shell script
docker run -p 4317:4317 -p 4318:4318 --rm -v $(pwd)/collector-config.yaml:/etc/otelcol/config.yaml otel/opentelemetry-collector
```


## 3 - Rode o App java-favorite
Use o maven para buildar ou se preferir rode diretamente na sua IDE:

## Créditos e fonte de Estudo:

https://github.com/davidgeorgehope/java-favorite/tree/main