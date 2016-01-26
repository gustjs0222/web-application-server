package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import sun.misc.IOUtils;
import util.HttpRequestUtils;
import util.UrlUtils;

public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

	private Socket connection;

	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
	}

	public void run() {
		log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
				connection.getPort());

		try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
			// TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
			DataOutputStream dos = new DataOutputStream(out);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			UrlUtils urlHandler = new UrlUtils();
			String line = br.readLine();
			String url = urlHandler.getUrlName(line);
			if (url.startsWith("/create")) {
				join(dos, urlHandler, br, line);

			} else if (url.startsWith("/login.html")) {
			
			} else {
				htmlHandler(dos, urlHandler, url);
			}

		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void htmlHandler(DataOutputStream dos, UrlUtils urlHandler, String url) throws IOException {
		byte[] body = null;
		body = urlHandler.readHtmlFile(url);
		response200Header(dos, body.length, false);
		responseBody(dos, body);
	}

	private void join(DataOutputStream dos, UrlUtils urlHandler, BufferedReader br, String line) throws IOException {
		byte[] body = null;

		String[] str = null;
		while (!"".equals(line)) {
			line = br.readLine();
			if (line.startsWith("Content-Length")) {
				str = line.split(" ");
				System.out.println(str[1]);
				break;
			}
			if (line == null)
				break;
		}

		storeUser(util.IOUtils.readData(br, Integer.parseInt(str[1])));

		response302Header(dos);
		body = urlHandler.readHtmlFile("/index.html");
		responseBody(dos, body);
	}

	private void storeUser(String url) {
		Map<String, String> userData = HttpRequestUtils.parseQueryString(url);
		User user = new User(userData.get("userId"), userData.get("password"), userData.get("name"), userData.get("email"));
		DataBase.addUser(user);
	}

	private void response200Header(DataOutputStream dos, int lengthOfBodyContent, Boolean b) {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
			dos.writeBytes("Set-Cookie: logined=" + b + "\r\n");
			dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void response302Header(DataOutputStream dos) {
		try {
			dos.writeBytes("HTTP/1.1 302 Found\r\n");
			dos.writeBytes("Location: ./index.html \r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void responseBody(DataOutputStream dos, byte[] body) {
		try {
			dos.write(body, 0, body.length);
			dos.writeBytes("\r\n");
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
}
