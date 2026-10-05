package com.project.store;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "app.jwt.secret=AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8=",
        "app.jwt.expiration=PT1H"
})
class StoreApplicationTests {

    @Test
    void contextLoads() {
    }
}