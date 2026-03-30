package compiler;

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
	public Void visitNode(ProgLetInNode node) {
		if (this.print) {
			this.printNode(node);
		}

		Map<String, STentry> clDecHashMap = new HashMap<>();
		this.symTable.add(clDecHashMap);
		for (Node cl : node.clDecList) {
			visit(cl);
		}

		Map<String, STentry> decHashMap = new HashMap<>();
		this.symTable.add(decHashMap);
		for (Node dec : node.decList) {
			visit(dec);
		}

		visit(node.exp);
		symTable.removeFirst();
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

	// ---------- Object Oriented Programming ----------

	@Override
	public Void visitNode(ClassNode classNode) {
		if (this.print) {
			printNode(classNode);
		}

		// ---------- Symbol Table Class Insertion ----------
		//TODO: Forse aggiungere un controllo sul nesting level
		Map<String, STentry> global = this.symTable.get(this.nestingLevel);
		ClassTypeNode classType = new ClassTypeNode(
			new ArrayList<>(),
			new ArrayList<>()
		);
		STentry classEntry = new STentry(
			this.nestingLevel,
			classType,
			this.decOffset--
		);

		if (global.put(classNode.className, classEntry) != null) {
			System.out.println(
				"Class ID " + classNode.className +
				" at line " + classNode.getLine() +
				" already declared"
			);
			this.stErrors++;
		}

		// ---------- Class Table Class Insertion ----------
		Map<String, STentry> virtualTable = new HashMap<>();
		this.classTable.put(classNode.className, virtualTable);

		// ---------- Entering Class Declaration ----------
		this.nestingLevel++;
		this.symTable.add(virtualTable);
		//TODO: Controlla anche qui gli offset
		int prevOffset = this.decOffset;
		this.decOffset = 0;
		int fieldOffset = -1;

		// Retrieve the Fields and Method Types
		for (FieldNode fieldNode : classNode.fields) {
			STentry fieldEntry = new STentry(
				this.nestingLevel,
				fieldNode.getType(),
				fieldOffset--
			);
			if (virtualTable.put(fieldNode.name, fieldEntry) != null) {
				System.out.println(
					"Field ID " + fieldNode.name +
					" at line " + fieldNode.getLine() +
					" already declared"
				);
			}

			classType.allFields.add(fieldNode.getType());
		}

		// TODO: Controlla caso in cui metodo chiama metodo
		for (MethodNode methodNode : classNode.methods) {
			// Visit the method  to add it to the ST, then add it to the virtual table
			this.visit(methodNode); //TODO: Prova accept
			classType.allMethods.add(methodNode.getType());
		}

		// Reset of the System Table, Nesting Level and Offset
    this.symTable.remove(this.nestingLevel);
		this.nestingLevel--;
		this.decOffset = prevOffset;
		return null;
	}

	@Override
	public Void visitNode(MethodNode methodNode) {
		if (this.print) {
			printNode(methodNode);
		}

		Map<String, STentry> currentScope = this.symTable.get(this.nestingLevel);
		ArrowTypeNode methodType = new ArrowTypeNode(
			methodNode.parList.stream().map(ParNode::getType).toList(),
			methodNode.retType
		);
		STentry entry = new STentry(
			this.nestingLevel,
			methodType,
			this.decOffset--
		);

		//TODO: TEST
		methodNode.offset = this.decOffset;

		// Insert into Symbol Table
		if (currentScope.put(methodNode.name, entry) != null) {
			System.out.println(
				"Fun id " + methodNode.name +
				" at line " + methodNode.getLine() +
				" already declared"
			);
			this.stErrors++;
		}

		// Creating a new Map for the Symbol Table
		nestingLevel++;
		Map<String, STentry> map = new HashMap<>();
		symTable.add(map);

		// TODO: Verifica se necessario
		// methodNode.offset = entry.offset;

		int prevOffset = decOffset;
		this.decOffset = -2;
		int parOffset = 1;

		// Visiting Method Parameters
		for (ParNode par : methodNode.parList) {
			STentry parEntry = new STentry(
				this.nestingLevel,
				par.getType(),
				parOffset++
			);
			if (map.put(par.id, parEntry) != null) {
				System.out.println(
					"Par id " + par.id +
					" at line " + methodNode.getLine() +
					" already declared"
				);
				stErrors++;
			}
		}

		// Visit the local declarations
		for (DecNode dec : methodNode.decList) {
			this.visit(dec); //TODO: Prova con accept
		}

		// Visit the body of the method
		this.visit(methodNode.exp); //TODO: Prova con accept

		// Reset of the System Table, Nesting Level and Offset
		this.symTable.remove(this.nestingLevel);
		this.nestingLevel--;
		this.decOffset = prevOffset;
		return null;
	}

	@Override
	public Void visitNode(ClassCallNode classCallNode) {
		if (this.print) {
			printNode(classCallNode);
		}

		// ---------- Class Lookup ----------
		STentry entry = this.stLookup(classCallNode.objId);

		if (entry == null) {
			System.out.println(
				"Object ID " + classCallNode.objId +
				" at line "+ classCallNode.getLine() +
				" not declared"
			);
			this.stErrors++;
		} else {
			if (!(entry.type instanceof RefTypeNode)) {
				System.out.println(
					"Object ID " + classCallNode.objId +
					" at line " + classCallNode.getLine() +
					" is not an Object"
				);
				this.stErrors++;
			} else {
				classCallNode.entry = entry;
			}
		}

		// ---------- Method Lookup ----------
		entry = this.classTable
			.get(classCallNode.objId)
			.get(classCallNode.methodId);

		if (entry == null) {
			System.out.println(
				"Method Call ID " + classCallNode.methodId +
				" at line " + classCallNode.getLine() +
				" not declared"
			);
			this.stErrors++;
		} else {
			classCallNode.methodEntry = entry;
		}
		classCallNode.nestingLevel = this.nestingLevel;
		for (Node arg : classCallNode.argList) {
			this.visit(arg);
		}
		return null;
	}

	@Override
	public Void visitNode(NewNode newNode) {
		if (this.print) {
			printNode(newNode);
		}

		if (!this.classTable.containsKey(newNode.classId)) {
			System.out.println(
				"Class ID " + newNode.classId +
				" at line " + newNode.getLine() +
				" not declared"
			);
			this.stErrors++;
		} else {
			newNode.entry = this.symTable.getFirst().get(newNode.classId);
		}

		for (Node arg : newNode.argList) {
			this.visit(arg);
		}
		return null;
	}

	@Override
	public Void visitNode(EmptyNode emptyNode) {
		if (this.print) {
			printNode(emptyNode);
		}

		return null;
	}

	@Override
	public Void visitNode(RefTypeNode refTypeNode) {
		if (this.print) {
			this.printNode(refTypeNode);
		}

		STentry entry = this.stLookup(refTypeNode.id);
		if (entry == null) {
			System.out.println(
				"Class Id " + refTypeNode.id +
				" at line " + refTypeNode.getLine() +
				" not declared"
			);
			stErrors++;
		} else {
			refTypeNode.entry = entry;
			//refTypeNode.nl = nestingLevel;
		}

		return null;
	}
}
