package ma.jmbb;

import java.io.File;
import java.util.List;
import ma.tools2.args.*;
import ma.tools2.args.conv.*;
import ma.tools2.util.NotImplementedException;

class Args extends AbstractArgs { // TODO z Problem: MaArgs speaks German

	static final String APPLICATION_NAME =
					"Java Ma_Sys.ma Block Based Backup";
	static final String APPLICATION_VERSION = "1.0.1.5";
	static final String COPYRIGHT_YEARS = "2013, 2014, 2015, 2017";

	final Parameter<File>            dstDatabase;
	final Parameter<List<File>>      srcUpdate;

	final Parameter<File>            srcDatabase;
	final Parameter<File>            dstMirror;

	final Parameter<File>            dstRestore;
	final Parameter<String>          pattern;
	final Parameter<List<File>>      srcRestore;
	final Parameter<RestorationMode> restorationMode;
	final Parameter<Integer>         restoreVersion;

	final Parameter<File>            editDatabase;

	Args() {
		super();

		// To have all parameters final, we need to initialize them in
		// the constructor.

		// -- Database Update --
		ArgumentList update = new ArgumentList(
			"Update the main database.", ArgumentComposition.AND
		);
		update.add(dstDatabase = new Parameter<File>(
			"dst-database", 'o',
			null, new NamedFileConverter("DB"),
			"Destination database."
		));
		update.add(srcUpdate = new Parameter<List<File>>(
			"src-update", 'i', null,
			new MultipleSourceConverter(),
			"Source files and directories separated with a space."
		));
		add(update);

		// -- Mirror --
		ArgumentList mirror = new ArgumentList(
			"Mirror the main database to a specific directory. " +
			"The most recent password is not copied.",
			ArgumentComposition.AND
		);
		mirror.add(srcDatabase = new Parameter<File>(
			"src-database", 'd', null,
			new NamedSourceDirectoryConverter("DB"),
			"Source database."
		));
		mirror.add(dstMirror = new Parameter<File>(
			"dst-mirror", 'c', null, new NamedFileConverter("DST"),
			"Destination to mirror to."
		));
		add(mirror);

		// -- Restoring --
		ArgumentList restore = new ArgumentList(
			"Restore files from any source.",
			ArgumentComposition.AND
		);
		restore.add(dstRestore = new Parameter<File>(
			"restore-to", 'r', null, new NamedFileConverter("DST"),
			"Destination to restore to."
		));
		ArgumentList patternList = new ArgumentList(null,
						ArgumentComposition.OPTIONAL);
		patternList.add(pattern = new Parameter<String>(
			"pattern", 'p', null, new StringConverter(),
			"If given, only filenames matching the REGEX given " +
								"are extracted."
			));
		restore.add(patternList);
		ArgumentList rml = new ArgumentList(null,
						ArgumentComposition.OPTIONAL);
		rml.add(restorationMode = new Parameter<RestorationMode>(
			"rmode", 't',
			RestorationMode.RESTORE_CONSISTENT,
			new RestorationModeConverter(),
			"Determines how to choose files for restoration:\n" +
			"consistent tries to restore a consistent set of " +
			"files.\nnew tries to restore the newest files " +
			"available.\nlist only lists all versions of files." +
			"You will normally use this in combination with a " +
			"pattern."
		));
		restore.add(rml);
		ArgumentList rvl = new ArgumentList(null,
						ArgumentComposition.OPTIONAL);
		rvl.add(restoreVersion = new Parameter<Integer>(
			"version", 'v', -1, new IntegerConverter(true),
			"Version of files to list or restore. -1 means the " +
			"newest version is chosen."
		));
		restore.add(rvl);
		restore.add(srcRestore = new Parameter<List<File>>(
			"sources", 's', null, new MultipleSourceConverter(),
			"Sources to restore from (space separated)."
		));
		add(restore);

		// -- Edit --
		ArgumentList editL = new ArgumentList("Edit given database " +
				"interactively.", ArgumentComposition.AND);
		editL.add(editDatabase = new Parameter<File>(
			"edit", 'e', null,
			new NamedSourceDirectoryConverter("DB"),
			"Database to edit."
		));
		add(editL);
	}

	protected String getApplicationName() {
		return APPLICATION_NAME;
	}

	protected String getApplicationVersion() {
		return APPLICATION_VERSION;
	}

	protected String getCopyrightYears() {
		return COPYRIGHT_YEARS;
	}

	protected String getInvocationCommand() {
		return "java ma.jmbb.Main";
	}

	ProgramMode determineMode() {
		if(srcUpdate.isSet()) {
			return ProgramMode.BACKUP_UPDATE_DB;
		} else if(srcDatabase.isSet()) {
			return ProgramMode.MIRROR;
		} else if(srcRestore.isSet()) {
			return ProgramMode.RESTORE;
		} else if(editDatabase.isSet()) {
			return ProgramMode.EDIT;
		} else {
			throw new NotImplementedException();
		}
	}

	protected String[] getCopyrightLines() {
		final String[] in = super.getCopyrightLines();
		return new String[] {
			in[0], in[1],
			"This program's encryption functions are " +
							"modifications of ",
			"Java AESCrypt, Copyright 2008 Vócali Sistemas " +
								"Inteligentes.",
			"For further information refer to http://" +
				"www.aescrypt.com/java_aes_crypt.html.",
			""
		};
	}

}
