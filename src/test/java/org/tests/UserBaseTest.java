package org.tests;

import config.RestAssuredConfigurator;
import org.testng.annotations.BeforeClass;

public class UserBaseTest {

    @BeforeClass(alwaysRun = true)
    public void setup() {
        RestAssuredConfigurator.configure("/api/users");

    }
}
