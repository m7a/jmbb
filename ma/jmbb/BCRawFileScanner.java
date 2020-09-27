package ma.jmbb;

import java.io.File;
import java.io.IOException;

import java.nio.file.*;
import java.nio.file.attribute.*;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

// BC: Backup Creator
class BCRawFileScanner extends AbstractFileScanner {

	private static final long CHECK_FOR_COMPLETED_EVERY_MS = 300;
	private static final String REQ_ATT = "unix:mode,lastModifiedTime,size";

	private final LinkedBlockingQueue<Stat> internalQueue;
	private final boolean showWarning;

	BCRawFileScanner(final PrintfIO o, final List<File> src)
						throws MBBFailureException {
		super(o, src);
		internalQueue = new LinkedBlockingQueue<Stat>();
		showWarning   = shallShowWarning();
	}

	private boolean shallShowWarning() {
		String raw = System.getenv("JMBB_WINDOWS");
		return raw == null || !raw.equals("true");
	}

	@Override
	public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes a)
							throws IOException {
		stat(d, a);
		return FileVisitResult.CONTINUE;
	}

	private void stat(Path path, BasicFileAttributes a) throws IOException {
		addStat(createStat(path, a));
	}

	Stat createStat(Path p, BasicFileAttributes a) throws IOException {
		final Map<String, Object> statRaw = readRawAttributes(p);
		return createStat(p, statRaw, a);
	}

	private Map<String, Object> readRawAttributes(Path path)
							throws IOException {
		try {
			return Files.readAttributes(path, REQ_ATT,
						LinkOption.NOFOLLOW_LINKS);
		} catch(IOException ex) {
			throw ex;
		} catch(UnsupportedOperationException ex) {
			if(showWarning) {
				o.edprintf(ex, "STAT unsupported: %s.\n",
							path.toString());
			}
			return null;
		}
	}

	private static Stat createStat(Path path, Map<String, Object> statRaw,
				BasicFileAttributes a) throws IOException {
		final String absoluteName = path.toAbsolutePath().toString();
		try {
			if(statRaw == null) {
				return new Stat(absoluteName, a);
			} else {
				return new Stat(absoluteName, statRaw);
			}	
		} catch(MBBFailureException ex) {
			throw new IOException(NEVER, ex);
		}
	}

	private void addStat(Stat s) throws IOException {
		try {
			internalQueue.put(s);
		} catch(InterruptedException ex) {
			throw new IOException(NEVER,
						new MBBFailureException(ex));
		}
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes a)
							throws IOException {
		stat(file, a);
		return FileVisitResult.CONTINUE;
	}

	/**
	 * If this returns null, there is NO element left to be processed. The
	 * Thread has likely already terminated then.
	 */
	Stat requestNextEntry() throws InterruptedException {
		Stat s;
		do {
			s = internalQueue.poll(CHECK_FOR_COMPLETED_EVERY_MS,
							TimeUnit.MILLISECONDS);
		} while(!isComplete() && s == null);
		return s;
	}

}
