package de.moonflower.jfritz.autoupdate;

public class UpdateFile {
	private String name;

	private String hash;

	private int size;

	public UpdateFile(String name, String hash, int size) {
		this.name = name;
		this.hash = hash;
		this.size = size;
	}

	public String getHash() {
		return hash;
	}

	public String getName() {
		return name;
	}

	public int getSize() {
		return size;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof UpdateFile))
			return false;

		UpdateFile otherFile = (UpdateFile) obj;
		if ((this.getName().equals(otherFile.getName()))
				&& (this.getHash().equals(otherFile.getHash()))
				&& (this.getSize() == otherFile.getSize()))
			return true;

		return false;
	}
}
