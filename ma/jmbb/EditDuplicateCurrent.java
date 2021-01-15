package ma.jmbb;

import java.util.*;

class EditDuplicateCurrent extends EditorAbstractCriticalDBCommand
			implements KeyValueReplacementHandler<String,DBFile> {

	private final Map<String,List<DBFile>> dupcur;

	EditDuplicateCurrent(PrintfIO o, DB db) {
		super(o, db);
		dupcur = new HashMap<String,List<DBFile>>();
	}

	@Override
	public String getCommandName() {
		return "dupcur";
	}

	@Override
	public String getArgsString() {
		return "[-d]";
	}

	@Override
	public String getDescription() {
		return "DANGER! Detect and delete duplicate current files.";
	}

	@Override
	public void call(String[] args) throws Exception {
		List<DBFile> dell = validateRedundantProcessingData();
		if(args.length == 2 && args[1].equals("-d")) {
			o.printf("Delete list (File: Version)\n");
			int n = 0;
			for(DBFile f: dell) {
				o.printf("%s: %d\n", f.getPath(), f.version);
				n++;
			}
			String response = o.readLn(
				"Type `del` to continue deleting these %d " +
				"files in the versions listed above ", n);
			if(response.equals("del"))
				obsoleteFiles(dell);
		}
	}

	/** @return suggested files to be obsoleted. */
	private List<DBFile> validateRedundantProcessingData() {
		List<DBFile> deleteList = new ArrayList<DBFile>();
		dupcur.clear(); // clear for safety
		Map<String,DBFile> nonObsolete = new AddOnceMap<String,DBFile>(
					new HashMap<String,DBFile>(), this);
		Map<String,DBFile> obsoleteNewest =
						new HashMap<String,DBFile>();
		db.blocks.fillRedundantProcessingData(nonObsolete,
								obsoleteNewest);
		int ndupcur = 0;
		for(Map.Entry<String,List<DBFile>> i: dupcur.entrySet()) {
			o.printf("Duplicate current files for %s:\n",
								i.getKey());
			// TODO z This just choses the last entry...
			Iterator<DBFile> iter = i.getValue().iterator();
			while(iter.hasNext()) {
				DBFile f = iter.next();
				o.printf("  (*) %s %s\n", RDateFormatter.format(
					f.modificationTime), f.formatXML());
				if(iter.hasNext())
					deleteList.add(f);
			}
			ndupcur++;
		}
		o.printf("%d duplicate current files detected.\n", ndupcur);
		dupcur.clear(); // save memory by clearing immediately
		return deleteList;
	}

	@Override
	public void handleKeyValueReplaced(String key, DBFile oldValue,
							DBFile newValue) {
		List<DBFile> dv = dupcur.get(key);
		if(dv == null) {
			dv = new ArrayList<DBFile>();
			dv.add(oldValue);
			dupcur.put(key, dv);
		}
		dv.add(newValue);
	}

	private void obsoleteFiles(List<DBFile> dell) throws Exception {
		for(DBFile f: dell)
			f.obsolete();

		try {
			for(DBBlock blk: db.blocks)
				blk.deleteIfNewlyObsolete(o);
		} finally {
			saveDBCritical();
		}
	}

	private static class AddOnceMap<K,V> implements Map<K,V> {

		private final KeyValueReplacementHandler<K,V>
							keyExistenceHandler;
		private final Map<K,V> sub;

		public AddOnceMap(Map<K,V> sub, KeyValueReplacementHandler<K,V>
							keyExistenceHandler) {
			super();
			this.keyExistenceHandler = keyExistenceHandler;
			this.sub = sub;
		}

		@Override public void clear() { sub.clear(); }
		@Override public boolean containsKey(Object key) {
					return sub.containsKey(key); }
		@Override public boolean containsValue(Object value) {
					return sub.containsValue(value); }
		@Override public Set<Map.Entry<K,V>> entrySet() {
					return sub.entrySet(); }
		@Override public boolean isEmpty() { return sub.isEmpty(); }
		@Override public Set<K> keySet() { return sub.keySet(); }
		@Override public V remove(Object key) {
						return sub.remove(key); }
		@Override public int size() { return sub.size(); }
		@Override public Collection<V> values() { return sub.values(); }
		@Override public V get(Object key) { return sub.get(key); }

		@Override
		public V put(K key, V value) {
			if(sub.containsKey(key))
				keyExistenceHandler.handleKeyValueReplaced(key,
							sub.get(key), value);
			return sub.put(key, value);
		}

		@Override
		public void putAll(Map<? extends K, ? extends V> m) {
			for(Map.Entry<? extends K, ? extends V> e: m.entrySet())
				put(e.getKey(), e.getValue());
		}

	}

}
