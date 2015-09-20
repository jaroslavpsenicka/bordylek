package org.bordylek.mon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource({"classpath:/service-context.xml", "classpath:/mon-context.xml",
    "classpath:/security-context.xml", "classpath:/rules-context.xml"})
public class MonApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(MonApplication.class, args);
    }

}
