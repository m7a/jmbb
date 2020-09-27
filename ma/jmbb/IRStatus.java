package ma.jmbb;

import static ma.jmbb.IRStatusSummary.*;

enum IRStatus {

	ACTIVE_VERIFIED             (OK,      "active,   verified"),
	ACTIVE_DUPLICATE_VERIFIED   (OK,      "active,   duplicate verified"),
	OBSOLETE_VERIFIED           (OK,      "obsolete, verified"),
	OBSOLETE_ABSENT             (OK,      "obsolete, absent"),
	OBSOLETE_DUPLICATE_VERIFIED (OK,      "obsolete, duplicate verified"),
	OBSOLETE_DUPLICATE_MISMATCH (WARNING, "obsolete, DUPLICATE MISMATCH"),
	NOT_IN_DATABASE             (WARNING, "          NOT IN DATABASE"),
	ACTIVE_DUPLICATE_MISMATCH   (WARNING, "active,   DUPLICATE MISMATCH"),
	UNKNOWN                     (FAILURE, "?,        unknown"),
	ACTIVE_ABSENT               (FAILURE, "active,   absent"),
	ACTIVE_CHECKSUM_MISMATCH    (FAILURE, "active,   CHECKSUM MISMATCH"),
	OBSOLETE_CHECKSUM_MISMATCH  (FAILURE, "obsolete, CHECKSUM MISMATCH");

	final IRStatusSummary summary;
	private final String  descr;

	private IRStatus(IRStatusSummary s, String descr) {
		this.summary = s;
		this.descr   = descr;
	}

	boolean isDuplicateMismatch() {
		return (this == OBSOLETE_DUPLICATE_MISMATCH) || 
					(this == ACTIVE_DUPLICATE_MISMATCH);
	}

	static IRStatus absent(boolean active) {
		return active? ACTIVE_ABSENT: OBSOLETE_ABSENT;
	}

	static IRStatus simpleComparison(boolean active, boolean match) {
		if(match) {
			return active? ACTIVE_VERIFIED: OBSOLETE_VERIFIED;
		} else {
			return active? ACTIVE_CHECKSUM_MISMATCH:
						OBSOLETE_CHECKSUM_MISMATCH;
		}
	}

	static IRStatus duplicate(boolean active, boolean match) {
		if(active) {
			return match? ACTIVE_DUPLICATE_VERIFIED:
						ACTIVE_DUPLICATE_MISMATCH;
		} else {
			return match? OBSOLETE_DUPLICATE_VERIFIED:
						OBSOLETE_DUPLICATE_MISMATCH;
		}
	}

	@Override
	public String toString() {
		return summary.toString() + "  " + descr;
	}

}
