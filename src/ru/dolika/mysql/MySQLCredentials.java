package ru.dolika.mysql;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class MySQLCredentials {
	private static final String USERPASSWORD_TXT = "userpassword.txt";
	public String address;
	public String username;
	public String password;
	public String database;

	public MySQLCredentials() throws IOException {
		Path path = Paths.get(USERPASSWORD_TXT);

		if (!Files.exists(path)) {
			Files.createFile(path);
		}
		List<String> lines = Files.lines(path).collect(Collectors.toList());
		if (lines.size() != 4) {
			return;
		}
		address = lines.get(0);
		username = lines.get(1);
		password = lines.get(2);
		database = lines.get(3);
	}

	@Override
	public String toString() {
		return "//" + username + ":" + password + "@" + address + "/" + database;
	}

	public static void main(String... strings) {
		try {
			System.out.println(new MySQLCredentials());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
