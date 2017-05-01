package ma.jmbb;

class AESCryptHeaderCheckFailureException extends MBBFailureException {

	AESCryptHeaderCheckFailureException() {
		super("Invalid AESCrypt file header.");
	}

}
