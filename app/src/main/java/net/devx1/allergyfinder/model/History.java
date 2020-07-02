package net.devx1.allergyfinder.model;

public class History {
	//history
	private String path, status, allergies;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAllergies() {
		return allergies;
	}

	public void setAllergies(String allergies) {
		this.allergies = allergies;
	}

	public History(String path, String status, String allergies) {
		this.path = path;
		this.status = status;
		this.allergies = allergies;
	}
}
