package de.moonflower.jfritz.utils.reverselookup;

public class ParseItem implements Comparable<ParseItem> {
	private ParseItemType type = ParseItemType.UNKNOWN;
	private String value = "";
	private int line = -1;
	private int startIndex = -1;

	public ParseItem(final ParseItemType type) {
		this.type = type;
	}

	public ParseItemType getType() {
		return this.type;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public boolean equals(Object o) {
		boolean result = false;

		if (o == null) {
			result = false;
		} else if (o instanceof ParseItem) {
			ParseItem other = (ParseItem)o;
			result = (this.line == other.getLine())
			      && (this.startIndex == other.getStartIndex())
			      && (this.type == other.getType())
			      && (this.value == other.getValue());
		}
		return result;
	}

	/**
	 * Note: this class has a natural ordering that is inconsistent with equals.
	 */
	@Override
	public int compareTo(ParseItem o) {
		if (this.getLine() > o.getLine()) {
			return 1;
		} else if (this.getLine() < o.getLine()) {
			return -1;
		} else {
			// lines are equal, sort by startIndex
			if (this.getStartIndex() > o.getStartIndex()) {
				return 1;
			} else if (this.getStartIndex() < o.getStartIndex()) {
				return -1;
			} else {
				// line and startIndex are equal, return equal
				return 0;
			}
		}
	}
}
