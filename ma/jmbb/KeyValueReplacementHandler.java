package ma.jmbb;

interface KeyValueReplacementHandler<K,V> {

	void handleKeyValueReplaced(K ok, V ov, V nv);

}
