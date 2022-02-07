package org.mascara.notifier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MascaraNotifierV2Application {

	public static void main(String[] args) {
		SpringApplication.run(MascaraNotifierV2Application.class, args);
	}

}
