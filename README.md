# Обмен валют — Currency Exchange

REST API для работы с валютами и обменными курсами на Java без использования Spring.

## Возможности

- Получение и добавление валют
- Просмотр, добавление и обновление обменных курсов
- Конвертация валют по прямому, обратному и кросс-курсу
- Обработка ошибок с соответствующими HTTP-кодами
- Простой веб-интерфейс

## Технологии

- Java 21, Jakarta Servlets
- Maven, Apache Tomcat 11
- SQLite, JDBC, HikariCP
- Gson
- HTML, CSS, JavaScript

## API

| Метод | Endpoint                               | Описание          |
|-------|----------------------------------------|-------------------|
| GET   | `/currencies`                          | Список валют      |
| GET   | `/currency/EUR`                        | Получение валюты  |
| POST  | `/currencies`                          | Добавление валюты |
| GET   | `/exchangeRates`                       | Список курсов     |
| GET   | `/exchangeRate/USDRUB`                 | Получение курса   |
| POST  | `/exchangeRates`                       | Добавление курса  |
| PATCH | `/exchangeRate/USDRUB`                 | Обновление курса  |
| GET   | `/exchange?from=USD&to=EUR&amount=100` | Конвертация валют |

## Запуск

Собрать проект:

```bash
mvn clean package
```

Разместить `target/currency-exchange.war` в папке `webapps` сервера Tomcat 11.

## Демо

[Открыть приложение](http://48.102.173.45:8080/currency-exchange/)

## Цель проекта

Практика Java Servlets, REST API, SQL, JDBC, многослойной архитектуры и деплоя на удалённый Linux-сервер.