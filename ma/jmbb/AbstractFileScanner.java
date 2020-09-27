package ma.jmbb;

import java.io.File;
import java.io.IOException;

import java.nio.file.*;
import java.nio.file.attribute.*;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

abstract class AbstractFileScanner extends FailableThread
						implements FileVisitor<Path> {

	static final String NEVER =
		"This text should never be visible to the user because the " +
		"\"Cause\" is the only relevant part of this Exception.";

	final PrintfIO o; // use only in subclass
	private final List<File> src;
	private boolean complete;

	AbstractFileScanner(PrintfIO o, List<File> src)
						throws MBBFailureException {
		super();
		this.o   = o;
		this.src = src;
		complete = false;
	}

	@Override
	public void runFailable() throws Exception {
		try {
			performSourceDirectoryScan();
		} finally {
			complete = true;
		}
	}

	void performSourceDirectoryScan() throws MBBFailureException {
		try {
			for(File i: src) {
				// REM does not follow symlinks which we want.
				Files.walkFileTree(i.toPath(), this);
			}
		} catch(UnsupportedOperationException ex) {
			throw new MBBFailureException("A required operation " +
					"is not available on this OS.", ex);
		} catch(IOException ex) {
			final Throwable c = ex.getCause();
			if(c != null && c instanceof MBBFailureException) {
				throw (MBBFailureException)c;
			} else {
				throw new MBBFailureException(ex);
			}
		}
	}

	@Override
	public FileVisitResult postVisitDirectory(Path d, IOException es)
							throws IOException {
		if(es != null) {
			throw es;
		}
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException es)
							throws IOException {
		o.edprintf(es, "Failed to visit: %s\n", file.toString());
		return FileVisitResult.CONTINUE;
	}

	boolean isComplete() {
		return complete;
	}

}
