package ma.jmbb;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.FileVisitResult;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

class IRFileScanner extends AbstractFileScanner {

	private final DB db;
	private final Map<Long,IRBlock> results;
	private final Executor checksummer;

	IRFileScanner(PrintfIO o, List<File> roots, DB db,
				Map<Long,IRBlock> results, Executor checksummer)
				throws MBBFailureException {
		super(o, roots);
		this.db          = db;
		this.results     = results;
		this.checksummer = checksummer;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes a)
							throws IOException {
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes a)
							throws IOException {
		String fn = file.getFileName().toString();
		if(fn.endsWith(".cxe")) {
			// Found a block
			long id = Long.parseLong(fn.substring(0,
							fn.indexOf('.')), 16);
			IRBlock irb;
			try {
				irb = new IRBlock(file, id, db);
			} catch(MBBFailureException ex) {
				throw new IOException(NEVER, ex);
			}
			results.put(id, irb);
			if(irb.isProcessingRequired()) {
				checksummer.execute(irb);
			}
		}
		return FileVisitResult.CONTINUE;
	}

}
