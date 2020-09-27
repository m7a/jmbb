package ma.jmbb;

enum IRStatusSummary {

	OK     ("[ ok ]"),
	WARNING("[warn]"),
	FAILURE("[FAIL]");

	private final String descr;

	private IRStatusSummary(String descr) {
		this.descr = descr;
	}

	@Override
	public String toString() {
		return descr;
	}

}
