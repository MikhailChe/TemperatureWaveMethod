package controller.mysql;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
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
			fillWithDefaults(path);
		}
		List<String> lines = Files.lines(path).collect(Collectors.toList());
		if (lines.size() != 4) {
			fillWithDefaults(path);
			return;
		}
		address = lines.get(0);
		username = lines.get(1);
		password = lines.get(2);
		database = lines.get(3);
	}

	public void fillWithDefaults(Path path) {
		try {
			Files.write(path, Arrays.asList("address", "username", "password", "database"), StandardOpenOption.CREATE);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public String toString() {
		return "jdbc:mysql://" + username + ":" + password + "@" + address + "/" + database;
	}

}
