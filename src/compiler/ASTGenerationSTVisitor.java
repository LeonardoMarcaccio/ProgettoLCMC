package compiler;

import java.util.*;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import compiler.AST.*;
import compiler.FOOLParser.*;
import compiler.lib.*;

import static compiler.lib.FOOLlib.*;

/**
 * Generates the structure for the AST
 */
public class ASTGenerationSTVisitor extends FOOLBaseVisitor<Node> {

	String indent;
	public boolean print;

	ASTGenerationSTVisitor() {}
	ASTGenerationSTVisitor(boolean debug) {
		this.print = debug;
	}

	private void printVarAndProdName(ParserRuleContext context) {
		String prefix="";
		Class<?> ctxClass = context.getClass();
		Class<?> parentClass = ctxClass.getSuperclass();

		// parentClass is the var context (and not ctxClass itself)
		if (!parentClass.equals(ParserRuleContext.class)) {
			prefix =
				lowerizeFirstChar(extractCtxName(parentClass.getName())) +
				": production #";
		}

		System.out.println(
			this.indent +
			prefix +
			lowerizeFirstChar(extractCtxName(ctxClass.getName()))
		);
	}
        
	@Override
	public Node visit(ParseTree tree) {
		if (tree == null) {
			return null;
		}

		String temp = this.indent;
		this.indent = (this.indent == null) ? "" : this.indent + "  ";

		Node result = super.visit(tree);
		this.indent = temp;

		return result;
	}

	@Override
	public Node visitProg(ProgContext context) {
		if (this.print) {
			this.printVarAndProdName(context);
		}

		return visit(context.progbody());
	}

	@Override
	public Node visitLetInProg(LetInProgContext context) {
		if (this.print) {
			this.printVarAndProdName(context);
		}

		List<DecNode> decList = new ArrayList<>();
		for (CldecContext clDec : context.cldec()) {
			decList.add((ClassNode) this.visit(clDec));
		}
		for (DecContext dec : context.dec()) {
			decList.add((DecNode) this.visit(dec));
		}

		return new ProgLetInNode(
			decList,
			this.visit(context.exp())
		);
	}

	@Override
	public Node visitNoDecProg(NoDecProgContext context) {
		if (this.print) {
			this.printVarAndProdName(context);
		}
		return new ProgNode(visit(context.exp()));
	}

	@Override
	public Node visitTimesDiv(TimesDivContext context) {
		if (this.print) {
			printVarAndProdName(context);
		}

		Node left = visit(context.exp(0));
		Node right = visit(context.exp(1));
		Node node;

		if (context.TIMES() != null) {
			node = new TimesNode(left, right);
			node.setLine(context.TIMES().getSymbol().getLine());
		} else {
			node = new DivNode(left, right);
			node.setLine(context.DIV().getSymbol().getLine());
		}

		return node;
	}

	@Override
	public Node visitPlusMinus(PlusMinusContext context) {
		if (this.print) {
			this.printVarAndProdName(context);
		}

		Node left = visit(context.exp(0));
		Node right = visit(context.exp(1));
		Node node;

		if (context.PLUS() != null) {
			node = new PlusNode(left, right);
			node.setLine(context.PLUS().getSymbol().getLine());
		} else {
			node = new MinusNode(left, right);
			node.setLine(context.MINUS().getSymbol().getLine());
		}

		return node;
	}

	@Override
	public Node visitComp(CompContext context) {
		if (this.print) {
			this.printVarAndProdName(context);
		}

		Node left = visit(context.exp(0));
		Node right = visit(context.exp(1));
		Node node;

		if (context.EQ() != null) {
			node = new EqualNode(left, right);
			node.setLine(context.EQ().getSymbol().getLine());
		} else if (context.GE() != null) {
			node = new GreaterEqualNode(left, right);
			node.setLine(context.GE().getSymbol().getLine());
		} else {
			node = new LessEqualNode(left, right);
			node.setLine(context.LE().getSymbol().getLine());
		}

		return node;
	}

	@Override
	public Node visitVardec(VardecContext context) {
		if (this.print) {
			this.printVarAndProdName(context);
		}

		Node node = null;

		if (context.ID()!=null) { //non-incomplete ST
			node = new VarNode(
				context.ID().getText(),
				(TypeNode) visit(context.type()),
				visit(context.exp())
			);
			node.setLine(context.VAR().getSymbol().getLine());
		}

		return node;
	}

	@Override
	public Node visitFundec(FundecContext context) {
		if (this.print) {
			this.printVarAndProdName(context);
		}

		List<ParNode> parList = new ArrayList<>();
		for (int i = 1; i < context.ID().size(); i++) {
			ParNode p = new ParNode(
				context.ID(i).getText(),
				(TypeNode) visit(context.type(i))
			);
			p.setLine(context.ID(i).getSymbol().getLine());
			parList.add(p);
		}

		List<DecNode> decList = new ArrayList<>();
		for (DecContext dec : context.dec()) {
			decList.add((DecNode) visit(dec));
		}

		Node node = null;
		if (!context.ID().isEmpty()) { //non-incomplete ST
			node = new FunNode(
				context.ID(0).getText(),
				(TypeNode)visit(context.type(0)),
				parList,
				decList,
				visit(context.exp())
			);
			node.setLine(context.FUN().getSymbol().getLine());
		}

		return node;
	}

	@Override
	public Node visitIntType(IntTypeContext context) {
		if (this.print) {
			this.printVarAndProdName(context);
		}

		return new IntTypeNode();
	}

	@Override
	public Node visitBoolType(BoolTypeContext context) {
		if (this.print) {
			this.printVarAndProdName(context);
		}

		return new BoolTypeNode();
	}

	@Override
	public Node visitInteger(IntegerContext context) {
		if (this.print) {
			this.printVarAndProdName(context);
		}

		int v = Integer.parseInt(context.NUM().getText());
		return new IntNode(context.MINUS() == null ? v : -v);
	}

	@Override
	public Node visitTrue(TrueContext context) {
		if (this.print) {
			this.printVarAndProdName(context);
		}

		return new BoolNode(true);
	}

	@Override
	public Node visitFalse(FalseContext context) {
		if (this.print) {
			this.printVarAndProdName(context);
		}

		return new BoolNode(false);
	}

	@Override
	public Node visitIf(IfContext context) {
		if (this.print) {
			this.printVarAndProdName(context);
		}

		Node ifNode = visit(context.exp(0));
		Node thenNode = visit(context.exp(1));
		Node elseNode = visit(context.exp(2));
		Node node = new IfNode(
			ifNode,
			thenNode,
			elseNode
		);

		node.setLine(context.IF().getSymbol().getLine());

		return node;
	}

	@Override
	public Node visitPrint(PrintContext context) {
		if (this.print) {
			this.printVarAndProdName(context);
		}

		return new PrintNode(visit(context.exp()));
	}

	@Override
	public Node visitPars(ParsContext context) {
		if (this.print) {
			this.printVarAndProdName(context);
		}

		return visit(context.exp());
	}

	@Override
	public Node visitId(IdContext context) {
		if (this.print) {
			this.printVarAndProdName(context);
		}

		Node node = new IdNode(context.ID().getText());
		node.setLine(context.ID().getSymbol().getLine());

		return node;
	}

	@Override
	public Node visitCall(CallContext context) {
		if (this.print) {
			this.printVarAndProdName(context);
		}

		List<Node> argList = new ArrayList<>();
		for (ExpContext arg : context.exp()) {
			argList.add(visit(arg));
		}

		Node node = new CallNode(context.ID().getText(), argList);
		node.setLine(context.ID().getSymbol().getLine());

		return node;
	}

	@Override
	public Node visitAndOr(AndOrContext context) {
		if (this.print) {
			this.printVarAndProdName(context);
		}

		Node left = visit(context.exp(0));
		Node right = visit(context.exp(1));
		Node node;

		if (context.AND() != null) {
			node = new AndNode(left, right);
			node.setLine(context.AND().getSymbol().getLine());
		} else {
			node = new OrNode(left, right);
			node.setLine(context.OR().getSymbol().getLine());
		}

		return node;
	}

	@Override
	public Node visitNot(NotContext context) {
		if (this.print) {
			this.printVarAndProdName(context);
		}

		Node node = new NotNode(visit(context.exp()));
		node.setLine(context.NOT().getSymbol().getLine());

		return node;
	}

	@Override
	public Node visitCldec(CldecContext context) {
		if (print) {
			this.printVarAndProdName(context);
		}

		/*
		 * Create the List of FieldNode by getting the i-th ID(Name of the argument)
		 * and visiting its corresponding TypeNode
		 */
		List<FieldNode> fieldList = new ArrayList<>();
		int typeOffset = 1; //TODO: To be modified in case of extension
		for (int i = 1; i < context.ID().size(); i++) {
			String nodeName = context.ID(i).getText();
			TypeNode nodeType = (TypeNode) visit(
				context.type(i - typeOffset)
			);
			FieldNode field = new FieldNode(
				nodeName,
				nodeType
			);
			field.setLine(context.ID(i).getSymbol().getLine());
			fieldList.add(field);
		}

		/*
		 * Create the List of Class specific Method declaration by iterating and visiting
		 * the list of MethdecContext
		 */
		List<MethodNode> methodList = new ArrayList<>();
		for (MethdecContext method : context.methdec()) {
			methodList.add((MethodNode) visit(method));
		}

		Node node = null;
		// Check the existance of an ID for the Class
		if (!context.ID().isEmpty()) {
			node = new ClassNode(
				context.ID(0).getText(),
				fieldList,
				methodList
			);
			node.setLine(context.CLASS().getSymbol().getLine());
		}

		return node;
	}

	@Override
	public Node visitMethdec(MethdecContext context) {
		if (this.print) {
			this.printVarAndProdName(context);
		}

		/*
		 * Create the List of Parameters by getting the i-th ID(Name of the argument)
		 * and visiting its corresponding TypeNode
		 */
		List<ParNode> parList = new ArrayList<>();
		for (int i = 1; i < context.ID().size(); i++) {
			ParNode par = new ParNode(
				context.ID(i).getText(),
				(TypeNode) visit(context.type(i))
			);
			par.setLine(context.ID(i).getSymbol().getLine());
			parList.add(par);
		}

    /*
     * Create the List of In-Method declarations by iterating and visiting
     * the list of DecContext
     */
    List<DecNode> decList = new ArrayList<>();
		for (DecContext dec : context.dec())
			decList.add((DecNode) visit(dec));

		Node node = null;
		// Check the existance of an ID for the Method
		if (!context.ID().isEmpty()) {
			node = new MethodNode(
				context.ID(0).getText(),
				(TypeNode) visit(context.type(0)),
				parList,
				decList,
				visit(context.exp())
			);
			node.setLine(context.FUN().getSymbol().getLine());
		}

		return node;
	}

	@Override
	public Node visitIdType(IdTypeContext context) {
		if (this.print) {
			this.printVarAndProdName(context);
		}

		return new RefTypeNode(context.ID().getText());
	}

	@Override
	public Node visitNull(NullContext context) {
		if (this.print) {
			this.printVarAndProdName(context);
		}

		EmptyNode node = new EmptyNode();
		node.setLine(context.NULL().getSymbol().getLine());

		return new EmptyNode();
	}

	@Override
	public Node visitDotCall(DotCallContext context) {
		if (this.print) {
			this.printVarAndProdName(context);
		}

		List<Node> argList = new ArrayList<>();
		for (ExpContext arg : context.exp()) {
			argList.add(visit(arg));
		}

		ClassCallNode node = new ClassCallNode(
			context.ID(0).getText(),
			context.ID(1).getText(),
			argList
		);
		node.setLine(context.ID(1).getSymbol().getLine());

		return node;
	}

	@Override
	public Node visitNew(NewContext context) {
		if (this.print) {
			this.printVarAndProdName(context);
		}

		List<Node> argList = new ArrayList<>();
		for (ExpContext field : context.exp()) {
			argList.add(visit(field));
		}

		NewNode node = new NewNode(
			context.ID().getText(),
			argList
		);

		return node;
	}
}
