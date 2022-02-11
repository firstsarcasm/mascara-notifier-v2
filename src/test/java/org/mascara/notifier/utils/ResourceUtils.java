package org.mascara.notifier.utils;

import lombok.NoArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class ResourceUtils {
	public static String resourceToString(String path) throws IOException {
		Resource resource = new ClassPathResource(path);
		return new String(Files.readAllBytes(resource.getFile().toPath()));
	}
}
