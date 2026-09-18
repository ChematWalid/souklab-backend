package com.project.souklab;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class SouklabApplicationTest {

    @Test
    void delegatesStartupToSpringApplication() {
        String[] arguments = {"--spring.main.web-application-type=none"};
        try (var springApplication = mockStatic(SpringApplication.class)) {
            springApplication.when(() -> SpringApplication.run(SouklabApplication.class, arguments))
                    .thenReturn(null);

            SouklabApplication.main(arguments);

            springApplication.verify(() -> SpringApplication.run(SouklabApplication.class, arguments));
        }
    }
}
