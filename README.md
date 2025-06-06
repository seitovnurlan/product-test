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


## ⚙️ Настройка окружения

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


## 📂 Структура проекта

```text                         
product-test/
├── .github/                               # Папка для конфигураций GitHub Actions
│   └── workflows/
│       ├── ci-cd.yml                     # CI/CD пайплайн: автоматическое деплоирование и тестирование
│       └── test.yml                       # Тестовый пайплайн: запуск тестов при пуше в репозиторий
├── k8s/                                   # Папка для конфигураций Kubernetes
│   └── deployment.yml                    # Конфигурация деплоймента приложения в Kubernetes
├── src/                                   # Основной исходный код
│   ├── main/
│   │   └── java/
│   │       └── testutil/
│   │           ├── CommonUtils.java       # Общие утилиты для работы с тестами и данными
│   │           └── MockDataGenerator.java # Генератор мок-данных для тестов
│   └── test/                              # Папка для тестов
│       └── java/
│           └── com/
│               └── qa/
│                   └── tests/
│                       ├── level1/
│                       │   └── QaLevel1Test.java  # API тесты для уровня 1
│                       ├── level2/
│                       │   └── QaLevel2Test.java  # API тесты для уровня 2
│                       └── level3/
│                           └── QaLevel3Test.java  # API тесты для уровня 3
│                       └── ProductClient.java      # Общий клиент для взаимодействия с API
│                       └── TestDataGenerator.java  # Генератор тестовых данных
│       └── resources/                     # Ресурсы, необходимые для тестов (например, конфигурации для Allure)
├── Dockerfile                             # Конфигурация Docker образа для запуска приложения
├── pom.xml                                # Maven конфигурация: зависимости и настройки
└── testing.xml                            # TestNG конфигурация для организации тестов и отчетности
                                                          