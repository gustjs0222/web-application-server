package util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class UrlUtils {
	public String getUrlName(String line) {
		
		String[] tokens = line.split(" ");
		return tokens[1];
	}
	
	public byte[] readHtmlFile(String url) throws IOException {
		return Files.readAllBytes(new File("./webapp" + url).toPath());
	}
}
