package com.lmora.cuentas.karate;

import com.intuit.karate.junit5.Karate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
class KarateTestRunner {

    @LocalServerPort
    private int port;

    @Karate.Test
    Karate cuentasApi() {
        System.setProperty("karate.baseUrl", "http://127.0.0.1:" + port);
        return Karate.run("clientes", "cuentas", "movimientos", "reportes").relativeTo(getClass());
    }
}
