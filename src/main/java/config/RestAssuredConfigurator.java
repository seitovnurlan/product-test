package config;

import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Настройщик RestAssured: базовый URL, порты, таймауты, charset и т.д.
 */
public class RestAssuredConfigurator {

    private static final Logger logger = LoggerFactory.getLogger(RestAssuredConfigurator.class);

    public static void configure(String basePath) {

        String baseUrl = System.getProperty("api.base.url", "http://localhost");
        int port = 31494;
        //Настройка RestAssured: baseURI, port, basePath

        RestAssured.baseURI = baseUrl;
        RestAssured.port = port;
        RestAssured.basePath = basePath;

        RestAssured.config = RestAssuredConfig.config()
                .encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("UTF-8"))
                .httpClient(HttpClientConfig.httpClientConfig()
//                        .setParam("http.connection.timeout", 5000)
//                        .setParam("http.socket.timeout", 5000)
//                        .setParam("http.connection-manager.timeout", 5000L)
                );

        // Включить логирование только при падениях
//        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}
