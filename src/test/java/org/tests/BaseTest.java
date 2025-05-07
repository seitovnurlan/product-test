package org.tests;

import io.restassured.RestAssured;
import org.testng.annotations.BeforeClass;

public class BaseTest {

    protected static final String BASE_URL = "http://localhost:31494";

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = BASE_URL;
    }
}

