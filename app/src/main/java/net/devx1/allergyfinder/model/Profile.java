package net.devx1.allergyfinder.model;

public class Profile {
	private String username, password, path;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Profile(String username, String password, String path) {
		this.username = username;
		this.password = password;
		this.path = path;
	}

	public Profile(String username, String password) {
		this.username = username;
		this.password = password;
	}
}
