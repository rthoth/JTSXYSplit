package com.github.rthoth.xysplit;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

public interface Download {

	default File download(URL url, String name) {
		File target = new File("build/download/".replace('/', File.separatorChar) + name);

		if (!target.exists()) {
			target.getParentFile().mkdirs();
			try (InputStream input = url.openConnection().getInputStream(); OutputStream output = new FileOutputStream(target)) {
				byte[] buffer = new byte[1024];
				int len = input.read(buffer);

				while (len != -1) {
					output.write(buffer, 0, len);
					len = input.read(buffer);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		return target;
	}

	default File download(String url, String name) {
		try {
			return download(new URL(url), name);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
}
