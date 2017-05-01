package ma.tools2.args;

import java.util.ArrayList;
import java.util.Iterator;

import ma.tools2.util.NotImplementedException;

// TODO POSS. nur noch eine factory-klasse
public class ArgumentList extends PartialArgs {

	private boolean containsList;
	private ArrayList<PartialArgs> backend;

	private ArgumentComposition comp;

	public ArgumentList(String description, ArgumentComposition comp) {
		super(description);
		backend   = new ArrayList<PartialArgs>();
		this.comp = comp;
	}

	public void add(PartialArgs p) {
		if(p instanceof ArgumentList && !containsList) {
			containsList = true;
		}
		backend.add(p);
	}

	void parse(InputParameter param) throws ParameterFormatException {
		Iterator<PartialArgs> subs = backend.iterator();
		while(subs.hasNext()) {
			subs.next().parse(param);
			if(param.isProcessed()) {
				return;
			}
		}
	}

	boolean isSet() {
		Iterator<PartialArgs> subs = backend.iterator();
		boolean ret = (comp == ArgumentComposition.AND);
		while(subs.hasNext()) {
			boolean sub = subs.next().isSet();
			switch(comp) {
			case AND:
				if(!sub) {
					return false;
				}
				break;
			case XOR:
				if(sub) {
					if(ret) {
						return false;
					} else {
						ret = true;
					}
				}
				break;
			case OPTIONAL:
				return true;
			default:
				throw new NotImplementedException();
			}
		}
		return ret;
	}

	protected void printHelp(int level) {
		int subLevel = level;
		if(!(containsList && comp == ArgumentComposition.OPTIONAL) ||
							hasDescription()) {
			printI(level, getUsage(), true);
			subLevel = level + 1;
		}
		if(hasDescription()) {
			System.out.println();
			super.printHelp(subLevel);
			System.out.println();
		}
		Iterator<PartialArgs> subs = backend.iterator();
		while(subs.hasNext()) {
			subs.next().printHelp(subLevel);
		}
	}

	protected String getUsage() {
		char[] braces = new char[2];
		if(comp == ArgumentComposition.OPTIONAL) {
			braces[0] = '[';
			braces[1] = ']';
		} else {
			braces[0] = '(';
			braces[1] = ')';
		}
		StringBuffer out = new StringBuffer();
		Iterator<PartialArgs> subs = backend.iterator();
		while(subs.hasNext()) {
			PartialArgs sub = subs.next();
			// Klammern wenn:
			// 1.) Unterliste und Unterliste keine Optionalliste
			// 2.) Optionalmodus gesetzt
			boolean brace = (sub instanceof ArgumentList &&
					(((ArgumentList)sub).comp !=
					ArgumentComposition.OPTIONAL)) ||
					(comp == ArgumentComposition.OPTIONAL);
			if(brace) {
				out.append(braces[0]);
			}
			out.append(sub.getUsage());
			if(brace) {
				out.append(braces[1]);
			}
			if(subs.hasNext()) {
				out.append(getCompSep());
			}
		}
		return out.toString();
	}

	private String getCompSep() {
		switch(comp) {
		case AND:
		case OPTIONAL:
			return " ";
		case XOR:
			return " | ";
		default:
			throw new NotImplementedException();
		}
	}

	/**
	 * Erzeugt eine Liste, deren enthaltene Argumente nicht vorhanden
	 * sein können oder von deren enthaltenen Argumenten genau eines
	 * gesetzt ist. Es handelt sich also um eine XOR Liste mit zusätzlicher
	 * "`nichts geset"' Option.
	 *
	 * @param description
	 * 	Beschreibung, wie sie auch der Constructor erwartet.
	 * @return
	 * 	Verschachtelte Listen, die der angegebenen Beschreibung
	 * 	entsprechen.
	 */
	public ArgumentList addOptionalXORList(String description) {
		ArgumentList optional = new ArgumentList(
			null, ArgumentComposition.OPTIONAL
		);
		ArgumentList xor = new ArgumentList(
			description, ArgumentComposition.XOR
		);
		optional.add(xor);
		add(optional);
		return xor;
	}

}
