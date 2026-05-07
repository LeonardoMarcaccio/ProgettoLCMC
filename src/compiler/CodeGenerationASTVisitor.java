package compiler;

import compiler.AST.*;
import compiler.lib.*;
import compiler.exc.*;
import svm.ExecuteVM;

import java.util.ArrayList;
import java.util.List;

import static compiler.lib.FOOLlib.*;

public class CodeGenerationASTVisitor extends BaseASTVisitor<String, VoidException> {

	CodeGenerationASTVisitor() {}
	CodeGenerationASTVisitor(boolean debug) {super(false,debug);} //enables print for debugging

	@Override
	public String visitNode(ProgLetInNode n) {
		if (print) printNode(n);
		String declCode = null;
		for (Node dec : n.decList) declCode=nlJoin(declCode,visit(dec));
		return nlJoin(
			"push 0",
			declCode, // generate code for declarations (allocation)
			visit(n.exp),
			"halt",
			getCode()
		);
	}

	@Override
	public String visitNode(ProgNode n) {
		if (print) printNode(n);
		return nlJoin(
			visit(n.exp),
			"halt"
		);
	}

	@Override
	public String visitNode(FunNode n) {
		if (print) printNode(n,n.id);
		String declCode = null, popDecl = null, popParl = null;
		for (Node dec : n.decList) {
			declCode = nlJoin(declCode,visit(dec));
			popDecl = nlJoin(popDecl,"pop");
		}
		for (int i = 0; i<n.parList.size(); i++) popParl = nlJoin(popParl,"pop");
		String funl = freshFunLabel();
		putCode(
			nlJoin(
				funl+":",
				"cfp", // set $fp to $sp value
				"lra", // load $ra value
				declCode, // generate code for local declarations (they use the new $fp!!!)
				visit(n.exp), // generate code for function body expression
				"stm", // set $tm to popped value (function result)
				popDecl, // remove local declarations from stack
				"sra", // set $ra to popped value
				"pop", // remove Access Link from stack
				popParl, // remove parameters from stack
				"sfp", // set $fp to popped value (Control Link)
				"ltm", // load $tm value (function result)
				"lra", // load $ra value
				"js"  // jump to popped address
			)
		);
		return "push "+funl;
	}

	@Override
	public String visitNode(VarNode n) {
		if (print) printNode(n,n.id);
		return visit(n.exp);
	}

	@Override
	public String visitNode(PrintNode n) {
		if (print) printNode(n);
		return nlJoin(
			visit(n.exp),
			"print"
		);
	}

	@Override
	public String visitNode(IfNode n) {
		if (print) {
			printNode(n);
		}
	 	String l1 = freshLabel();
	 	String l2 = freshLabel();
		return nlJoin(
			visit(n.cond),
			"push 1",
			"beq "+l1,
			visit(n.elseExp),
			"b "+l2,
			l1+":",
			visit(n.thenExp),
			l2+":"
		);
	}

	@Override
	public String visitNode(EqualNode n) {
		if (print) {
			printNode(n);
		}
	 	String l1 = freshLabel();
	 	String l2 = freshLabel();
		return nlJoin(
			visit(n.left),
			visit(n.right),
			"beq "+l1,
			"push 0",
			"b "+l2,
			l1+":",
			"push 1",
			l2+":"
		);
	}

	@Override
	public String visitNode(TimesNode n) {
		if (print) printNode(n);
		return nlJoin(
			visit(n.left),
			visit(n.right),
			"mult"
		);
	}

	@Override
	public String visitNode(PlusNode n) {
		if (print) printNode(n);
		return nlJoin(
			visit(n.left),
			visit(n.right),
			"add"
		);
	}

	@Override
	public String visitNode(CallNode n) {
		if (print) printNode(n,n.id);
		String argCode = null, getAR = null;
		for (int i=n.argList.size()-1;i>=0;i--) {
			argCode = nlJoin(argCode, visit(n.argList.get(i)));
		}
		for (int i = 0;i<n.nestingLevel-n.entry.nl;i++) {
			getAR = nlJoin(getAR, "lw");
		}

		return n.entry.offset < 0 ? nlJoin(
			"lfp", // load Control Link (pointer to frame of function "id" caller)
			argCode, // generate code for argument expressions in reversed order
			"lfp",
			getAR, // retrieve address of frame containing "id" declaration
			// by following the static chain (of Access Links)
			"stm", // set $tm to popped value (with the aim of duplicating top of stack)
			"ltm", // load Access Link (pointer to frame of function "id" declaration)
			"ltm", // duplicate top of stack
			"push "+n.entry.offset, "add", // compute address of "id" declaration
			"lw", // load address of "id" function
			"js"  // jump to popped address (saving address of subsequent instruction in $ra)
		) : nlJoin(
			"lfp", // load Control Link (pointer to frame of function "id" caller)
			argCode, // generate code for argument expressions in reversed order
			"lfp",
			getAR, // retrieve address of frame containing "id" declaration
			// by following the static chain (of Access Links)
			"stm", // set $tm to popped value (with the aim of duplicating top of stack)
			"ltm", // load Access Link (pointer to frame of function "id" declaration)
			"ltm", // duplicate top of stack
			"lw",
			"push "+n.entry.offset, "add", // compute address of "id" declaration
			"lw", // load address of "id" function
			"js"  // jump to popped address (saving address of subsequent instruction in $ra)
		);
	}

	@Override
	public String visitNode(IdNode n) {
		if (print) printNode(n,n.id);
		String getAR = null;
		for (int i = 0; i<n.nestingLevel -n.entry.nl; i++) getAR=nlJoin(getAR,"lw");
		return nlJoin(
			"lfp", getAR, // retrieve address of frame containing "id" declaration
			              // by following the static chain (of Access Links)
			"push "+n.entry.offset, "add", // compute address of "id" declaration
			"lw" // load value of "id" variable
		);
	}

	@Override
	public String visitNode(BoolNode n) {
		if (print) printNode(n,n.val.toString());
		return "push "+(n.val?1:0);
	}

	@Override
	public String visitNode(IntNode n) {
		if (print) printNode(n,n.val.toString());
		return "push "+n.val;
	}

	@Override
	public String visitNode(GreaterEqualNode n) {
		if (print) {
			printNode(n);
		}
		String l1 = freshLabel();
		String l2 = freshLabel();
		return nlJoin(
			visit(n.right),
			visit(n.left),
			"bleq " + l1,
			"push 0",
			"b " + l2,
			l1 + ":",
			"push 1",
			l2 + ":"
		);
	}

	@Override
	public String visitNode(LessEqualNode n) {
		if (print) {
			printNode(n);
		}
		String l1 = freshLabel();
		String l2 = freshLabel();
		return nlJoin(
			visit(n.left),
			visit(n.right),
			"bleq " + l1,
			"push 0",
			"b " + l2,
			l1 + ":",
			"push 1",
			l2 + ":"
		);
	}

	@Override
	public String visitNode(NotNode n) {
		if (print) {
			printNode(n);
		}
		String l1 = freshLabel();
		String l2 = freshLabel();
		return nlJoin(
			visit(n.expression),   // valuta espressione
			"push 1",
			"beq " + l1,    // se exp == 1 → vai a l1
			"push 1",       // caso exp == 0 → risultato = 1
			"b " + l2,
			l1 + ":",
			"push 0",       // caso exp == 1 → risultato = 0
			l2 + ":"
		);
	}

	@Override
	public String visitNode(MinusNode n) {
		if (print) {
			printNode(n);
		}
		return nlJoin(
			visit(n.left),
			visit(n.right),
			"sub"
		);
	}

	@Override
	public String visitNode(OrNode n) {
		if (print) {
			printNode(n);
		}
		String l1 = freshLabel();
		String l2 = freshLabel();
		return nlJoin(
			visit(n.left),
			"push 1",
			"beq " + l1,     // se left == 1 → true
			visit(n.right),
			"push 1",
			"beq " + l1,     // se right == 1 → true
			"push 0",        // entrambi false
			"b " + l2,
			l1 + ":",
			"push 1",
			l2 + ":"
		);
	}

	@Override
	public String visitNode(DivNode n) {
		if (print) printNode(n);
		return nlJoin(
			visit(n.left),
			visit(n.right),
			"div"
		);
	}

	@Override
	public String visitNode(AndNode n) {
		if (print) {
			printNode(n);
		}
		String l1 = freshLabel();
		String l2 = freshLabel();
		String l3 = freshLabel();
		return nlJoin(
			visit(n.left),
			"push 1",
			"beq " + l1,
			"push 0",
			"b " + l3,
			l1 + ":",
			visit(n.right),
			"push 1",
			"beq " + l2,
			"push 0",
			"b " + l3,
			l2 + ":",
			"push 1",
			l3 + ":"
		);
	}

	@Override
	public String visitNode(ClassNode node) {
		if (this.print) {
			this.printNode(node);
		}

		List<String> dispatchTable = new ArrayList<>();
		for (MethodNode method : node.methods) {
			visit(method);
			dispatchTable.add(method.label); // nuovo metodo
		}

		String code = "lhp";

		for (String label : dispatchTable) {
			code = nlJoin(
				code,
				"push " + label,
				"lhp",
				"sw",
				"lhp",
				"push 1",
				"add",
				"shp"
			);
		}

		return code; //NOTA : Stato finale della DT è "$hs"
	}

	@Override
	public String visitNode(MethodNode node) {
		if (this.print) {
			printNode(node, node.name);
		}

		String declCode = null, popDecl = null, popParl = null;

		for (Node dec : node.decList) {
			declCode = nlJoin(declCode,visit(dec));
			popDecl = nlJoin(popDecl,"pop");
		}

		for (int i=0;i<node.parList.size();i++) {
			popParl = nlJoin(popParl,"pop");
		}

		node.label = freshFunLabel();

		putCode(
			nlJoin(
				node.label + ":",
				"cfp", // set $fp to $sp value
				"lra", // load $ra value
				declCode, // generate code for local declarations (they use the new $fp!!!)
				visit(node.exp), // generate code for function body expression
				"stm", // set $tm to popped value (function result)
				popDecl, // remove local declarations from stack
				"sra", // set $ra to popped value
				"pop", // remove Access Link from stack
				popParl, // remove parameters from stack
				"sfp", // set $fp to popped value (Control Link)
				"ltm", // load $tm value (function result)
				"lra", // load $ra value
				"js"  // jump to popped address
			)
		);

		return null;
	}

	@Override
	public String visitNode(EmptyNode node) {
		if (this.print) {
			printNode(node);
		}
		return nlJoin("push -1"); //TODO: Check if it is necessary
	}

	@Override
	public String visitNode(ClassCallNode node) {
		if (this.print) {
			printNode(node);
		}

		String argCode = null;
		for (
			int i = node.argList.size() - 1;
			i>=0;
			i--
		) {
			argCode = nlJoin(
				argCode,
				visit(node.argList.get(i)));
		}

		String getActivationRecord = null;
		for (
			int i = 0;
			i < node.nestingLevel - node.entry.nl;
			i++
		) {
			getActivationRecord = nlJoin(getActivationRecord, "lw");
		}

		return nlJoin(
			"lfp", // load Control Link (pointer to frame of function "id" caller)
			argCode, // generate code for argument expressions in reversed order
			"lfp",
			getActivationRecord, // retrieve address of frame containing "id" declaration
			// by following the static chain (of Access Links)
			"push " + node.entry.offset,
			"add",
			"lw",
			"stm", // set $tm to popped value (with the aim of duplicating top of stack)
			"ltm", // load Access Link (pointer to frame of function "id" declaration)
			"ltm", // duplicate top of stack
			"lw",
			"push " + node.methodEntry.offset,
			"add", // compute address of "id" declaration
			"lw", // load address of "id" function
			"js"  // jump to popped address (saving address of subsequent instruction in $ra)
		);
	}

	@Override
	public String visitNode(NewNode node) {
		if (this.print) {
			this.printNode(node);
		}

		String code = null;

		for (Node arg : node.argList) {
			code = nlJoin(code, visit(arg));
		}

		for (Node arg : node.argList) {
			code = nlJoin(
				code,
				"lhp",
				"sw",
				"lhp",
				"push 1",
				"add",
				"shp"
			);
		}

		int offset = node.entry.offset + ExecuteVM.MEMSIZE;

		//NOTE: Carico il puntatore alla Dispatch Table corretta
		code = nlJoin(
			code,
			"push " + offset,
			"lw",
			"lhp",
			"sw",
			"lhp",
			"lhp",
			"push 1",
			"add",
			"shp"
		);

		return code;
	}
}
