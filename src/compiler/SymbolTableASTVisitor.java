package compiler;

import java.util.*;
import compiler.AST.*;
import compiler.exc.*;
import compiler.lib.*;

/**
 * The objective is to create and update the Symbol Table and its entries
 */
public class SymbolTableASTVisitor extends BaseASTVisitor<Void,VoidException> {

	private List<Map<String, STentry>> symTable = new ArrayList<>(); // Each element of the list is a scope of the program
	private Map<String, Map<String, STentry>> classTable = new HashMap<>(); // A Symbol Table used for the classes
	private int nestingLevel=0; // current nesting level
	private int decOffset=-2; // counter for offset of local declarations at current nesting level 
	int stErrors=0;

	SymbolTableASTVisitor() {}
	SymbolTableASTVisitor(boolean debug) { // enables print for debugging
		super(debug);
	}

	private STentry stLookup(String id) {
		int j = this.nestingLevel;
		STentry entry = null;

		while (j >= 0 && entry == null) {
			entry = this.symTable.get(j--).get(id);
		}

		return entry;
	}

	@Override
	public Void visitNode(ProgLetInNode node) {
		if (this.print) {
			this.printNode(node);
		}

		Map<String, STentry> decHashMap = new HashMap<>();
		this.symTable.add(decHashMap);
		for (Node dec : node.decList) {
			this.visit(dec);
		}

		this.visit(node.exp);
		this.symTable.removeFirst();

		return null;
	}

	@Override
	public Void visitNode(ProgNode node) {
		if (this.print) {
			this.printNode(node);
		}

		this.visit(node.exp);

		return null;
	}
	
	@Override
	public Void visitNode(FunNode node) {
		if (this.print) {
			this.printNode(node);
		}

		// Retrieving the current Scope
		Map<String, STentry> currentNestingLevel = this.symTable.get(this.nestingLevel);

		// Retrieving the Function Argument Types
		List<TypeNode> parTypes = new ArrayList<>();
		for (ParNode par : node.parList) {
			parTypes.add(par.getType());
		}

		// Creating the Entry for the Function
		STentry entry = new STentry(
			this.nestingLevel,
			new ArrowTypeNode(parTypes, node.retType),
			this.decOffset--
		);

		// Check if the ID is already in the symbol table
		if (currentNestingLevel.put(node.id, entry) != null) {
			System.out.println(
				"Fun id " + node.id +
				" at line " + node.getLine() +
				" already declared"
			);
			this.stErrors++;
		}

		// Entering a Function creates a new scope
		this.nestingLevel++;
		Map<String, STentry> newNestingLevel = new HashMap<>();
		this.symTable.add(newNestingLevel);

		// Stores counter for offset of declarations at previous nesting level
		int prevNLDecOffset = this.decOffset;
		this.decOffset = -2;
		int parOffset = 1;

		for (ParNode par : node.parList) {
			if (
				newNestingLevel.put(
					par.id,
					new STentry(
						this.nestingLevel,
						par.getType(),
						parOffset++)
				) != null
			) {
				System.out.println(
					"Par id " + par.id +
					" at line " + node.getLine() +
					" already declared");
				this.stErrors++;
			}
		}

		// Visiting the inner function declaration
		for (Node dec : node.decList) {
			this.visit(dec);
		}

		// Visiting the Main Function Body
		this.visit(node.exp);

		// Delete the function scope after exit, due to it being useless after
		this.symTable.remove(this.nestingLevel--);
		this.decOffset = prevNLDecOffset;
		return null;
	}
	
	@Override
	public Void visitNode(VarNode node) {
		if (this.print) {
			this.printNode(node);
		}

		this.visit(node.exp);
		Map<String, STentry> hm = this.symTable.get(this.nestingLevel);
		STentry entry = new STentry(
			this.nestingLevel,
			node.getType(),
			this.decOffset--
		);

		//inserimento di ID nella symtable
		if (hm.put(node.id, entry) != null) {
			System.out.println(
				"Var id " + node.id +
				" at line " + node.getLine() +
				" already declared"
			);
			this.stErrors++;
		}

		return null;
	}

	@Override
	public Void visitNode(PrintNode node) {
		if (this.print) {
			this.printNode(node);
		}

		this.visit(node.exp);

		return null;
	}

	@Override
	public Void visitNode(IfNode node) {
		if (this.print) {
			this.printNode(node);
		}

		this.visit(node.cond);
		this.visit(node.thenExp);
		this.visit(node.elseExp);

		return null;
	}
	
	@Override
	public Void visitNode(EqualNode node) {
		if (this.print) {
			this.printNode(node);
		}

		this.visit(node.left);
		this.visit(node.right);

		return null;
	}
	
	@Override
	public Void visitNode(TimesNode node) {
		if (this.print) {
			this.printNode(node);
		}

		this.visit(node.left);
		this.visit(node.right);

		return null;
	}
	
	@Override
	public Void visitNode(PlusNode node) {
		if (this.print) {
			this.printNode(node);
		}

		this.visit(node.left);
		this.visit(node.right);

		return null;
	}

	@Override
	public Void visitNode(CallNode node) {
		if (this.print) {
			this.printNode(node);
		}

		STentry entry = this.stLookup(node.id);

		if (entry == null) {
			System.out.println(
				"Fun id " + node.id +
				" at line " + node.getLine() +
				" not declared"
			);
			this.stErrors++;
		} else {
			node.entry = entry;
			node.nestingLevel = this.nestingLevel;
		}

		for (Node arg : node.argList) {
			this.visit(arg);
		}

		return null;
	}

	@Override
	public Void visitNode(IdNode node) {
		if (this.print) {
			this.printNode(node);
		}

		STentry entry = this.stLookup(node.id);
		if (entry == null) {
			System.out.println(
				"Var or Par id " + node.id +
				" at line " + node.getLine() +
				" not declared"
			);
			this.stErrors++;
		} else {
			node.entry = entry;
			node.nestingLevel = this.nestingLevel;
		}

		return null;
	}

	@Override
	public Void visitNode(BoolNode node) {
		if (this.print) {
			this.printNode(
				node,
				node.val.toString()
			);
		}

		return null;
	}

	@Override
	public Void visitNode(IntNode node) {
		if (this.print) {
			this.printNode(
				node,
				node.val.toString()
			);
		}

		return null;
	}

	@Override
	public Void visitNode(GreaterEqualNode node) {
		if (this.print) {
			this.printNode(node);
		}

		this.visit(node.left);
		this.visit(node.right);

		return null;
	}

	@Override
	public Void visitNode(LessEqualNode node) {
		if (this.print) {
			this.printNode(node);
		}

		this.visit(node.left);
		this.visit(node.right);

		return null;
	}

	@Override
	public Void visitNode(NotNode node) {
		if (this.print) {
			this.printNode(node);
		}

		this.visit(node.expression);

		return null;
	}

	@Override
	public Void visitNode(MinusNode node) {
		if (this.print) {
			this.printNode(node);
		}

		this.visit(node.left);
		this.visit(node.right);

		return null;
	}

	@Override
	public Void visitNode(OrNode node) {
		if (this.print) {
			this.printNode(node);
		}

		this.visit(node.left);
		this.visit(node.right);

		return null;
	}

	@Override
	public Void visitNode(DivNode node) {
		if (this.print) {
			this.printNode(node);
		}

		this.visit(node.left);
		this.visit(node.right);

		return null;
	}

	@Override
	public Void visitNode(AndNode node) {
		if (this.print) {
			this.printNode(node);
		}

		this.visit(node.left);
		this.visit(node.right);

		return null;
	}

	// ---------- Object Oriented Programming ----------

	@Override
	public Void visitNode(ClassNode node) {
		if (this.print) {
			this.printNode(node);
		}

		// ---------- Symbol Table Class Insertion ----------
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

		if (global.put(node.className, classEntry) != null) {
			System.out.println(
				"Class ID " + node.className +
				" at line " + node.getLine() +
				" already declared"
			);
			this.stErrors++;
		}

		// ---------- Class Table Class Insertion ----------
		Map<String, STentry> virtualTable = new HashMap<>();
		this.classTable.put(node.className, virtualTable);

		// ---------- Entering Class Scope ----------
		this.nestingLevel++;
		this.symTable.add(virtualTable);
		int prevOffset = this.decOffset;
		this.decOffset = 0;
		int fieldOffset = -1;

		// Retrieve the Fields and Method Types
		for (FieldNode fieldNode : node.fields) {
			STentry fieldEntry = new STentry(
				this.nestingLevel,
				fieldNode.getType(),
				fieldOffset--
			);

			fieldNode.offset = fieldEntry.offset;

			if (virtualTable.put(fieldNode.name, fieldEntry) != null) {
				System.out.println(
					"Field ID " + fieldNode.name +
					" at line " + fieldNode.getLine() +
					" already declared"
				);
			}

			classType.allFields.add(fieldNode.getType());
		}

		// Visit a method and adding it to the ST and the Virtual Table
		for (MethodNode methodNode : node.methods) {
			this.visit(methodNode);
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
			this.printNode(methodNode);
		}

		Map<String, STentry> currentScope = this.symTable.get(this.nestingLevel);
		ArrowTypeNode methodType = new ArrowTypeNode(
			methodNode.parList.stream().map(ParNode::getType).toList(),
			methodNode.retType
		);
		STentry entry = new STentry(
			this.nestingLevel,
			methodType,
			this.decOffset++
		);

		// Insert into Symbol Table
		if (currentScope.put(methodNode.name, entry) != null) {
			System.out.println(
				"Fun id " + methodNode.name +
				" at line " + methodNode.getLine() +
				" already declared"
			);
			this.stErrors++;
		}

		methodNode.offset = entry.offset;

		// Creating a new Map for the Symbol Table
		this.nestingLevel++;
		Map<String, STentry> map = new HashMap<>();
		this.symTable.add(map);

		int prevOffset = this.decOffset;
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
				this.stErrors++;
			}
		}

		// Visit the local declarations
		for (DecNode dec : methodNode.decList) {
			this.visit(dec);
		}

		// Visit the body of the method
		this.visit(methodNode.exp);

		// Reset of the System Table, Nesting Level and Offset
		this.symTable.remove(this.nestingLevel);
		this.nestingLevel--;
		this.decOffset = prevOffset;
		return null;
	}

	@Override
	public Void visitNode(ClassCallNode classCallNode) {
		if (this.print) {
			this.printNode(classCallNode);
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

				// ---------- Method Lookup ----------
				RefTypeNode type = (RefTypeNode) entry.type;
				STentry methodEntry = this.classTable
					.get(type.id)
					.get(classCallNode.methodId);

				if (methodEntry == null) {
					System.out.println(
						"Method Call ID " + classCallNode.methodId +
							" at line " + classCallNode.getLine() +
							" not declared"
					);
					this.stErrors++;
				} else {
					classCallNode.methodEntry = methodEntry;
				}
			}
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
			this.printNode(newNode);
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
			this.printNode(emptyNode);
		}

		return null;
	}
}
