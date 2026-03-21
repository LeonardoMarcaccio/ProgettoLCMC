package compiler;

import java.lang.reflect.Type;
import java.util.*;
import compiler.AST.*;
import compiler.exc.*;
import compiler.lib.*;

public class SymbolTableASTVisitor extends BaseASTVisitor<Void,VoidException> {

	// TODO: Segnati questa cosa negli appunti
	private List<Map<String, STentry>> symTable = new ArrayList<>();
	private Map<String, Map<String, STentry>> classTable = new HashMap<>();
	private int nestingLevel=0; // current nesting level
	private int decOffset=-2; // counter for offset of local declarations at current nesting level 
	int stErrors=0;

	SymbolTableASTVisitor() {}
	SymbolTableASTVisitor(boolean debug) {super(debug);} // enables print for debugging

	private STentry stLookup(String id) {
		int j = nestingLevel;
		STentry entry = null;
		while (j >= 0 && entry == null) 
			entry = symTable.get(j--).get(id);
		return entry;
	}

	@Override
	public Void visitNode(ProgLetInNode n) {
		if (print) printNode(n);
		Map<String, STentry> hm = new HashMap<>();
		symTable.add(hm);
	    for (Node dec : n.declist) visit(dec);
		visit(n.exp);
		symTable.removeFirst();
		return null;
	}

	@Override
	public Void visitNode(ProgNode n) {
		if (print) printNode(n);
		visit(n.exp);
		return null;
	}
	
	@Override
	public Void visitNode(FunNode n) {
		if (print) {
			printNode(n);
		}
		Map<String, STentry> hm = symTable.get(nestingLevel);
		List<TypeNode> parTypes = new ArrayList<>();  
		for (ParNode par : n.parlist) {
			parTypes.add(par.getType());
		}
		STentry entry = new STentry(nestingLevel, new ArrowTypeNode(parTypes,n.retType),decOffset--);
		//inserimento di ID nella symtable
		if (hm.put(n.id, entry) != null) {
			System.out.println("Fun id " + n.id + " at line "+ n.getLine() +" already declared");
			stErrors++;
		} 
		//creare una nuova hashmap per la symTable
		nestingLevel++;
		Map<String, STentry> hmn = new HashMap<>();
		symTable.add(hmn);
		int prevNLDecOffset=decOffset; // stores counter for offset of declarations at previous nesting level 
		decOffset=-2;
		
		int parOffset=1;
		for (ParNode par : n.parlist)
			if (hmn.put(par.id, new STentry(nestingLevel,par.getType(),parOffset++)) != null) {
				System.out.println("Par id " + par.id + " at line "+ n.getLine() +" already declared");
				stErrors++;
			}
		for (Node dec : n.declist) visit(dec);
		visit(n.exp);
		//rimuovere la hashmap corrente poiche' esco dallo scope               
		symTable.remove(nestingLevel--);
		decOffset=prevNLDecOffset; // restores counter for offset of declarations at previous nesting level 
		return null;
	}
	
	@Override
	public Void visitNode(VarNode n) {
		if (print) printNode(n);
		visit(n.exp);
		Map<String, STentry> hm = symTable.get(nestingLevel);
		STentry entry = new STentry(nestingLevel,n.getType(),decOffset--);
		//inserimento di ID nella symtable
		if (hm.put(n.id, entry) != null) {
			System.out.println("Var id " + n.id + " at line "+ n.getLine() +" already declared");
			stErrors++;
		}
		return null;
	}

	@Override
	public Void visitNode(PrintNode n) {
		if (print) printNode(n);
		visit(n.exp);
		return null;
	}

	@Override
	public Void visitNode(IfNode n) {
		if (print) printNode(n);
		visit(n.cond);
		visit(n.th);
		visit(n.el);
		return null;
	}
	
	@Override
	public Void visitNode(EqualNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}
	
	@Override
	public Void visitNode(TimesNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}
	
	@Override
	public Void visitNode(PlusNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(CallNode n) {
		if (print) printNode(n);
		STentry entry = stLookup(n.id);
		if (entry == null) {
			System.out.println("Fun id " + n.id + " at line "+ n.getLine() + " not declared");
			stErrors++;
		} else {
			n.entry = entry;
			n.nl = nestingLevel;
		}
		for (Node arg : n.arglist) visit(arg);
		return null;
	}

	@Override
	public Void visitNode(IdNode n) {
		if (print) printNode(n);
		STentry entry = stLookup(n.id);
		if (entry == null) {
			System.out.println("Var or Par id " + n.id + " at line "+ n.getLine() + " not declared");
			stErrors++;
		} else {
			n.entry = entry;
			n.nl = nestingLevel;
		}
		return null;
	}

	@Override
	public Void visitNode(BoolNode n) {
		if (print) printNode(n, n.val.toString());
		return null;
	}

	@Override
	public Void visitNode(IntNode n) {
		if (print) printNode(n, n.val.toString());
		return null;
	}

	@Override
	public Void visitNode(GreaterEqualNode greaterEqualNode) {
		if (print) printNode(greaterEqualNode);
		visit(greaterEqualNode.left);
		visit(greaterEqualNode.right);
		return null;
	}

	@Override
	public Void visitNode(LessEqualNode lessEqualNode) {
		if (print) printNode(lessEqualNode);
		visit(lessEqualNode.left);
		visit(lessEqualNode.right);
		return null;
	}

	@Override
	public Void visitNode(NotNode notNode) {
		if (print) printNode(notNode);
		visit(notNode.expression);
		return null;
	}

	@Override
	public Void visitNode(MinusNode minusNode) {
		if (print) printNode(minusNode);
		visit(minusNode.left);
		visit(minusNode.right);
		return null;
	}

	@Override
	public Void visitNode(OrNode orNode) {
		if (print) printNode(orNode);
		visit(orNode.left);
		visit(orNode.right);
		return null;
	}

	@Override
	public Void visitNode(DivNode dovNode) {
		if (print) printNode(dovNode);
		visit(dovNode.left);
		visit(dovNode.right);
		return null;
	}

	@Override
	public Void visitNode(AndNode andNode) {
		if (print) printNode(andNode);
		visit(andNode.left);
		visit(andNode.right);
		return null;
	}

	@Override
	public Void visitNode(ClassNode classNode) {
		if (this.print) {
			printNode(classNode);
		}

		// ---------- Symbol Table Class Insertion ----------
		Map<String, STentry> map = this.symTable.get(this.nestingLevel);
		ClassTypeNode classType = new ClassTypeNode(
			new ArrayList<>(),
			new ArrayList<>()
		);
		STentry classEntry = new STentry(
			this.nestingLevel,
			classType,
			this.decOffset-- // TODO = Controlla la correttezza dell'offset
		);

		if (map.put(classNode.className, classEntry) != null) {
			System.out.println(
				"Class ID " + classNode.className +
					" at line " + classNode.getLine() +
					" already declared"
			);
			this.stErrors++;
		}

		// ---------- Class Table First Class Insertion ----------
		Map<String, STentry> virtualTable = new HashMap<>();
		this.classTable.put(classNode.className, virtualTable);

		// ---------- Entering Class Declaration ----------
		this.nestingLevel++;
		this.symTable.add(virtualTable);
		//TODO: Controlla anche qui gli offset
		int prevOffset = decOffset;
		int fieldOffset = -1;

		// Retrieve the Fields and Method Types
		for (FieldNode fieldNode : classNode.fieldList) {
			STentry fieldEntry = new STentry(
				this.nestingLevel,
				fieldNode.getType(),
				fieldOffset
			);
			virtualTable.put(fieldNode.name, fieldEntry);
			classType.allFields.add(fieldNode.getType());
			fieldOffset--;
		}

		for (MethodNode methodNode : classNode.methodList) {
			// Visit the method  to add it to the ST, then add it to the virtual table
			methodNode.accept(this);
			STentry methodEntry = new STentry(
				this.nestingLevel,
				methodNode.getType(),
				decOffset
			);
			classType.allMethods.add(methodNode.getType());
		}
		/*
		 NOTA: Perché ho visitato i metodi dopo averli aggiunti tutti alla table?
		 Perché se:
		 class A {
    	 int x;
    	 int f() { return g(); }
    	 int g() { return x; }
		 }
		 Quando analizzi f, g non è ancora nella Symbol Table
		*/

		// Reset of the System Table, Nesting Level and Offset
    this.symTable.remove(nestingLevel);
		this.nestingLevel--;
		this.decOffset = prevOffset;
		return null;
	}

	@Override
	public Void visitNode(MethodNode methodNode) {
		if (print) printNode(methodNode);

		Map<String, STentry> currentScope = symTable.get(nestingLevel);

		// 🔹 tipo funzionale
		ArrowTypeNode methodType = new ArrowTypeNode(
			methodNode.parlist.stream().map(ParNode::getType).toList(),
			methodNode.retType
		);

		// 🔹 entry metodo
		STentry entry = new STentry(
			nestingLevel,
			methodType,
			decOffset++
		);

		currentScope.put(methodNode.id, entry);

		// 🔥 SALVO OFFSET DENTRO IL NODO
		methodNode.offset = entry.offset;

		// 🔹 nuovo scope metodo
		symTable.add(new HashMap<>());
		nestingLevel++;

		int prevOffset = decOffset;
		int parOffset = 1;

		// parametri
		for (ParNode p : methodNode.parlist) {
			symTable.get(nestingLevel).put(
				p.id,
				new STentry(nestingLevel, p.getType(), parOffset++)
			);
		}

		// dichiarazioni locali
		for (DecNode d : methodNode.declist) {
			d.accept(this);
		}

		// corpo
		methodNode.body.accept(this);

		// uscita
		symTable.remove(nestingLevel);
		nestingLevel--;
		decOffset = prevOffset;

		return null;
	}
}
