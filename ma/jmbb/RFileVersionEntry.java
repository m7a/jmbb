package ma.jmbb;

class RFileVersionEntry {

	final DBFile file;
	final DBBlock inBlk;
	
	RFileVersionEntry(DBFile file, DBBlock inBlk) {
		super();
		this.file  = file;
		this.inBlk = inBlk;
	}

}
