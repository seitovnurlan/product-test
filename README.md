# 🚀 Product Test Automation Framework

![Java](https://img.shields.io/badge/Java-17%2B-blue)
![TestNG](https://img.shields.io/badge/TestNG-7.6-red)
![Docker](https://img.shields.io/badge/Docker-24.0%2B-2496ED)
![Kubernetes](https://img.shields.io/badge/Kubernetes-1.28%2B-326CE5)
![Maven](https://img.shields.io/badge/Maven-3.9%2B-C71A36)

> Современный фреймворк для автоматизированного тестирования API и UI с поддержкой CI/CD и оркестрацией в Kubernetes

## 📌 Содержание
- [🚀 О проекте](#-о-проекте)
- [🛠 Технологический стек](#-технологический-стек)
- [📂 Структура проекта](#-структура-проекта)
- [⚙️ Настройка окружения](#️-настройка-окружения)
- [🚦 Запуск тестов](#-запуск-тестов)
- [🐳 Docker & Kubernetes](#-docker--kubernetes)
- [📊 Отчеты](#-отчеты)
- [🤝 Как внести вклад](#-как-внести-вклад)
- [📞 Контакты](#-контакты)

## 🚀 О проекте

Этот проект представляет собой профессиональный фреймворк для автоматизированного тестирования, который включает:
- **API тесты** (User, Product, Order)
- **UI тесты** (на базе Selenium WebDriver)
- **Генерацию тестовых данных**
- **CI/CD пайплайны** (GitHub Actions)
- **Оркестрацию в Kubernetes**
- **Allure отчеты**

## 🛠 Технологический стек

| Компонент       | Технология          | Версия   |
|-----------------|---------------------|----------|
| Язык           | Java                | 17+      |
| Фреймворк      | TestNG              | 7.6+     |
| Сборка         | Maven               | 3.9+     |
| Контейнеризация| Docker              | 24.0+    |
| Оркестрация    | Kubernetes          | 1.28+    |
| Отчеты         | Allure              | 2.23+    |
| CI/CD          | GitHub Actions      | -        |

## 📂 Структура проекта

```text
product-test/
├── .github/
│   └── workflows/
│       ├── ci-cd.yml       # CI/CD пайплайн
│       └── test.yml        # Тестовый пайплайн
├── k8s/
│   └── deployment.yml      # Конфиг деплоймента в Kubernetes
├── src/
│   ├── main/
│   │   └── java/
│   │       └── utils/
│   │           ├── CommonUtils.java       # Общие утилиты
│   │           └── MockDataGenerator.java # Генератор мок-данных
│   └── test/
│       ├── java/
│       │   ├── api/                       # API тесты
│       │   │   ├── OrderApiTest.java
│       │   │   ├── ProductApiTest.java
│       │   │   └── UserApiTest.java
│       │   ├── config/                    # Конфигурация
│       │   │   ├── DriverFactory.java     # Инициализация WebDriver
│       │   │   └── TestConfig.java        # Настройки тестов
│       │   ├── core/
│       │   │   └── BaseTest.java          # Базовый тестовый класс
│       │   ├── pages/                     # Page Objects
│       │   │   └── LoginPage.java         # Страница логина
│       │   └── utils/
│       │       └── TestDataGenerator.java # Генератор тестовых данных
│       └── resources/                     # Ресурсы
├── Dockerfile              # Конфигурация Docker образа
├── pom.xml                 # Maven конфигурация
└── testing.xml             # TestNG конфигурация

⚙️ Настройка окружения

🔹 Требования

JDK 17+ (скачать)
Maven 3.9+ (инструкция)
Docker 24.0+ (установка)
kubectl (для Kubernetes) (установка)
Minikube (для локального Kubernetes) (установка)
🔹 Установка

bash
# Клонировать репозиторий
git clone https://github.com/seitovnurlan/product-test.git
cd product-test

# Собрать проект
mvn clean install
🚦 Запуск тестов

🔸 Локальный запуск

bash
# Все тесты
mvn test

# Конкретный тестовый класс
mvn test -Dtest=UserApiTest

# С определенной TestNG группой
mvn test -Dgroups=smoke
🔸 Параметры запуска

Параметр	Описание	Пример
-Dbrowser	Браузер для UI тестов	-Dbrowser=chrome
-Denv	Окружение (dev/stage/prod)	-Denv=stage
-DthreadCount	Количество потоков	-DthreadCount=3
🐳 Docker & Kubernetes

🔹 Сборка Docker образа

bash
docker build -t product-test:latest .
🔹 Запуск в Docker

bash
docker run -e ENV=stage product-test
🔹 Развертывание в Kubernetes

Запустите Minikube:
bash
minikube start
Примените конфигурацию:
bash
kubectl apply -f k8s/deployment.yml
Проверьте статус:
bash
kubectl get pods -w
Доступ к отчетам:
bash
kubectl port-forward <pod-name> 8080:8080
Отчеты будут доступны по адресу: http://localhost:8080

📊 Отчеты

После выполнения тестов отчеты Allure генерируются автоматически:

🔸 Просмотр отчетов локально

bash
mvn allure:serve
🔸 Генерация статического отчета

bash
mvn allure:report
Отчет будет доступен в: target/site/allure-maven-plugin/index.html

🤝 Как внести вклад

Форкните репозиторий
Создайте ветку (git checkout -b feature/your-feature)
Сделайте коммит (git commit -am 'Add some feature')
Запушьте ветку (git push origin feature/your-feature)
Создайте Pull Request
📞 Контакты

👤 Nurlan Seitov
📧 seitov@gmail.com
🌍 GitHub seitovnurlan

<div align="center"> <sub>Создано с ❤️ для автоматизированного тестирования</sub> </div> ```