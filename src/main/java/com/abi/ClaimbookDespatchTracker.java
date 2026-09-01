package com.abi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class ClaimbookDespatchTracker {

    static {
        // Windows resolves "India Standard Time" to the deprecated alias "Asia/Calcutta",
        // which some Postgres builds don't recognize during the connection handshake.
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
    }

    public static void main(final String[] args) {
        SpringApplication.run(ClaimbookDespatchTracker.class, args);
    }
}
