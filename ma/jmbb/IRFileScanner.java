package ma.jmbb;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.FileVisitResult;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;

class IRFileScanner extends AbstractFileScanner {

	private final Map<Long,IRBlock> results;

	IRFileScanner(PrintfIO o, List<File> roots, Map<Long,IRBlock> results)
						throws MBBFailureException {
		super(o, roots);
		this.results = results;
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
			long id = Long.parseLong(fn.substring(0,
							fn.indexOf('.')), 16);
			if(results.containsKey(id)) {
				results.get(id).addFile(file);
			} else {
				results.put(id, new IRBlock(id, file));
			}
		}
		return FileVisitResult.CONTINUE;
	}

}
