package compiler;

import compiler.AST.*;
import compiler.lib.*;
import compiler.exc.*;

public class PrintEASTVisitor extends BaseEASTVisitor<Void,VoidException> {

	PrintEASTVisitor() { super(false,true); } 

	@Override
	public Void visitNode(ProgLetInNode n) {
		printNode(n);
		for (Node dec : n.decList) visit(dec);
		visit(n.exp);
		return null;
	}

	@Override
	public Void visitNode(ProgNode n) {
		printNode(n);
		visit(n.exp);
		return null;
	}

	@Override
	public Void visitNode(FunNode n) {
		printNode(n,n.id);
		visit(n.retType);
		for (ParNode par : n.parlist) visit(par);
		for (Node dec : n.declist) visit(dec);
		visit(n.exp);
		return null;
	}

	@Override
	public Void visitNode(ParNode n) {
		printNode(n,n.id);
		visit(n.getType());
		return null;
	}

	@Override
	public Void visitNode(VarNode n) {
		printNode(n,n.id);
		visit(n.getType());
		visit(n.exp);
		return null;
	}

	@Override
	public Void visitNode(PrintNode n) {
		printNode(n);
		visit(n.exp);
		return null;
	}

	@Override
	public Void visitNode(IfNode n) {
		printNode(n);
		visit(n.cond);
		visit(n.th);
		visit(n.el);
		return null;
	}

	@Override
	public Void visitNode(EqualNode n) {
		printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(TimesNode n) {
		printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(PlusNode n) {
		printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(CallNode n) {
		printNode(n,n.id+" at nestinglevel "+n.nl); 
		visit(n.entry);
		for (Node arg : n.arglist) visit(arg);
		return null;
	}

	@Override
	public Void visitNode(IdNode n) {
		printNode(n,n.id+" at nestinglevel "+n.nl); 
		visit(n.entry);
		return null;
	}

	@Override
	public Void visitNode(BoolNode n) {
		printNode(n,n.val.toString());
		return null;
	}

	@Override
	public Void visitNode(IntNode n) {
		printNode(n,n.val.toString());
		return null;
	}
	
	@Override
	public Void visitNode(ArrowTypeNode n) {
		printNode(n);
		for (Node par: n.parlist) visit(par);
		visit(n.ret,"->"); //marks return type
		return null;
	}

	@Override
	public Void visitNode(BoolTypeNode n) {
		printNode(n);
		return null;
	}

	@Override
	public Void visitNode(IntTypeNode n) {
		printNode(n);
		return null;
	}
	
	@Override
	public Void visitSTentry(STentry entry) {
		printSTentry("nestlev "+entry.nl);
		printSTentry("type");
		visit(entry.type);
		printSTentry("offset "+entry.offset);
		return null;
	}

	@Override
	public Void visitNode(GreaterEqualNode node) {
		printNode(node);
		visit(node.left);
		visit(node.right);
		return null;
	}

	@Override
	public Void visitNode(LessEqualNode node) {
		printNode(node);
		visit(node.left);
		visit(node.right);
		return null;
	}

	@Override
	public Void visitNode(NotNode node) {
		printNode(node);
		visit(node.expression);
		return null;
	}

	@Override
	public Void visitNode(MinusNode node) {
		printNode(node);
		visit(node.left);
		visit(node.right);
		return null;
	}

	@Override
	public Void visitNode(OrNode node) {
		printNode(node);
		visit(node.left);
		visit(node.right);
		return null;
	}

	@Override
	public Void visitNode(DivNode node) {
		printNode(node);
		visit(node.left);
		visit(node.right);
		return null;
	}

	@Override
	public Void visitNode(AndNode node) {
		printNode(node);
		visit(node.left);
		visit(node.right);
		return null;
	}

	// ---------- Object Oriented Programming ----------

	@Override
	public Void visitNode(ClassNode node) {
		printNode(node);
		visit(node.getType());
		for (FieldNode field : node.fields) {
			visit(field);
		}
		for (MethodNode method : node.methods) {
			visit(method);
		}
		return null;
	}

	@Override
	public Void visitNode(FieldNode node) {
		printNode(node);
		visit(node.getType());
		return null;
	}

	@Override
	public Void visitNode(MethodNode node) {
		printNode(node);
		visit(node.getType());
		for (Node par : node.parList) {
			visit(par);
		}
		for (Node dec : node.decList) {
			visit(dec);
		}
		visit(node.exp);
		return null;
	}

	@Override
	public Void visitNode(ClassCallNode node) {
		printNode(node);
		visitSTentry(node.entry);
		visitSTentry(node.methodEntry);
		for (Node arg : node.argList) {
			visit(arg);
		}
		return null;
	}

	@Override
	public Void visitNode(NewNode node) {
		printNode(node);
		visitSTentry(node.entry);
		for (Node arg : node.argList) {
			visit(arg);
		}
		return null;
	}

	@Override
	public Void visitNode(EmptyNode node) {
		printNode(node);
		return null;
	}

	@Override
	public Void visitNode(ClassTypeNode node) {
		printNode(node);
		for (Node field : node.allFields) {
			visit(field);
		}
		for (Node method : node.allMethods) {
			visit(method);
		}
		return null;
	}

	@Override
	public Void visitNode(RefTypeNode node) {
		printNode(node);
		return null;
	}

	@Override
	public Void visitNode(EmptyTypeNode node) {
		printNode(node);
		return null;
	}
}
