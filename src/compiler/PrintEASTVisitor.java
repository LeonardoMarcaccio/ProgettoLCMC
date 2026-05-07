package compiler;

import compiler.AST.*;
import compiler.lib.*;
import compiler.exc.*;

public class PrintEASTVisitor extends BaseEASTVisitor<Void,VoidException> {

	PrintEASTVisitor() { super(false,true); } 

	@Override
	public Void visitNode(ProgLetInNode node) {
		this.printNode(node);
		for (Node dec : node.decList) {
			this.visit(dec);
		}
		this.visit(node.exp);
		return null;
	}

	@Override
	public Void visitNode(ProgNode node) {
		this.printNode(node);
		this.visit(node.exp);
		return null;
	}

	@Override
	public Void visitNode(FunNode node) {
		this.printNode(node,node.id);
		this.visit(node.retType);
		for (ParNode par : node.parList) {
			this.visit(par);
		}
		for (Node dec : node.decList) {
			this.visit(dec);
		}
		this.visit(node.exp);
		return null;
	}

	@Override
	public Void visitNode(ParNode node) {
		this.printNode(node,node.id);
		this.visit(node.getType());
		return null;
	}

	@Override
	public Void visitNode(VarNode node) {
		this.printNode(node,node.id);
		this.visit(node.getType());
		this.visit(node.exp);
		return null;
	}

	@Override
	public Void visitNode(PrintNode node) {
		this.printNode(node);
		this.visit(node.exp);
		return null;
	}

	@Override
	public Void visitNode(IfNode node) {
		this.printNode(node);
		this.visit(node.cond);
		this.visit(node.thenExp);
		this.visit(node.elseExp);
		return null;
	}

	@Override
	public Void visitNode(EqualNode node) {
		this.printNode(node);
		this.visit(node.left);
		this.visit(node.right);
		return null;
	}

	@Override
	public Void visitNode(TimesNode node) {
		this.printNode(node);
		this.visit(node.left);
		this.visit(node.right);
		return null;
	}

	@Override
	public Void visitNode(PlusNode node) {
		this.printNode(node);
		this.visit(node.left);
		this.visit(node.right);
		return null;
	}

	@Override
	public Void visitNode(CallNode node) {
		this.printNode(
			node,
			node.id + " at nestinglevel " + node.nestingLevel
		);
		this.visit(node.entry);
		for (Node arg : node.argList) {
			this.visit(arg);
		}
		return null;
	}

	@Override
	public Void visitNode(IdNode node) {
		this.printNode(
			node,
			node.id + " at nestinglevel " + node.nestingLevel
		);
		this.visit(node.entry);
		return null;
	}

	@Override
	public Void visitNode(BoolNode node) {
		this.printNode(
			node,
			node.val.toString()
		);
		return null;
	}

	@Override
	public Void visitNode(IntNode node) {
		this.printNode(
			node,
			node.val.toString()
		);
		return null;
	}
	
	@Override
	public Void visitNode(ArrowTypeNode node) {
		this.printNode(node);
		for (Node par: node.parList) {
			this.visit(par);
		}
		this.visit(node.returnType,"->"); //marks return type
		return null;
	}

	@Override
	public Void visitNode(BoolTypeNode node) {
		this.printNode(node);
		return null;
	}

	@Override
	public Void visitNode(IntTypeNode node) {
		this.printNode(node);
		return null;
	}
	
	@Override
	public Void visitSTentry(STentry entry) {
		this.printSTentry("nestlev " + entry.nl);
		this.printSTentry("type");
		this.visit(entry.type);
		this.printSTentry("offset " + entry.offset);
		return null;
	}

	@Override
	public Void visitNode(GreaterEqualNode node) {
		this.printNode(node);
		this.visit(node.left);
		this.visit(node.right);
		return null;
	}

	@Override
	public Void visitNode(LessEqualNode node) {
		this.printNode(node);
		this.visit(node.left);
		this.visit(node.right);
		return null;
	}

	@Override
	public Void visitNode(NotNode node) {
		this.printNode(node);
		this.visit(node.expression);
		return null;
	}

	@Override
	public Void visitNode(MinusNode node) {
		this.printNode(node);
		this.visit(node.left);
		this.visit(node.right);
		return null;
	}

	@Override
	public Void visitNode(OrNode node) {
		this.printNode(node);
		this.visit(node.left);
		this.visit(node.right);
		return null;
	}

	@Override
	public Void visitNode(DivNode node) {
		this.printNode(node);
		this.visit(node.left);
		this.visit(node.right);
		return null;
	}

	@Override
	public Void visitNode(AndNode node) {
		this.printNode(node);
		this.visit(node.left);
		this.visit(node.right);
		return null;
	}

	// ---------- Object Oriented Programming ----------

	@Override
	public Void visitNode(ClassNode node) {
		this.printNode(node);
		this.visit(node.getType());
		for (FieldNode field : node.fields) {
			this.visit(field);
		}
		for (MethodNode method : node.methods) {
			this.visit(method);
		}
		return null;
	}

	@Override
	public Void visitNode(FieldNode node) {
		this.printNode(node);
		this.visit(node.getType());
		return null;
	}

	@Override
	public Void visitNode(MethodNode node) {
		this.printNode(node);
		this.visit(node.getType());
		for (Node par : node.parList) {
			this.visit(par);
		}
		for (Node dec : node.decList) {
			this.visit(dec);
		}
		this.visit(node.exp);
		return null;
	}

	@Override
	public Void visitNode(ClassCallNode node) {
		this.printNode(node);
		this.visitSTentry(node.entry);
		this.visitSTentry(node.methodEntry);
		for (Node arg : node.argList) {
			this.visit(arg);
		}
		return null;
	}

	@Override
	public Void visitNode(NewNode node) {
		this.printNode(node);
		this.visitSTentry(node.entry);
		for (Node arg : node.argList) {
			this.visit(arg);
		}
		return null;
	}

	@Override
	public Void visitNode(EmptyNode node) {
		this.printNode(node);
		return null;
	}

	@Override
	public Void visitNode(ClassTypeNode node) {
		this.printNode(node);
		for (Node field : node.allFields) {
			this.visit(field);
		}
		for (Node method : node.allMethods) {
			this.visit(method);
		}
		return null;
	}

	@Override
	public Void visitNode(RefTypeNode node) {
		this.printNode(node);
		return null;
	}

	@Override
	public Void visitNode(EmptyTypeNode node) {
		this.printNode(node);
		return null;
	}
}
