package compiler;

import compiler.AST.*;
import compiler.exc.*;
import compiler.lib.*;

import java.util.ArrayList;
import java.util.List;

import static compiler.TypeRels.*;

//visitNode(n) fa il type checking di un Node n e ritorna:
//- per una espressione, il suo tipo (oggetto BoolTypeNode o IntTypeNode)
//- per una dichiarazione, "null"; controlla la correttezza interna della dichiarazione
//(- per un tipo: "null"; controlla che il tipo non sia incompleto) 
//
//visitSTentry(s) ritorna, per una STentry s, il tipo contenuto al suo interno
public class TypeCheckEASTVisitor extends BaseEASTVisitor<TypeNode,TypeException> {

	TypeCheckEASTVisitor() { super(true); } // enables incomplete tree exceptions 
	TypeCheckEASTVisitor(boolean debug) { super(true,debug); } // enables print for debugging

	//checks that a type object is visitable (not incomplete) 
	private TypeNode ckvisit(TypeNode t) throws TypeException {
		visit(t);
		return t;
	} 
	
	@Override
	public TypeNode visitNode(ProgLetInNode node) throws TypeException {
		if (this.print) {
			this.printNode(node);
		}
		for (Node dec : node.decList)
			try {
				this.visit(dec);
			} catch (IncomplException exception) {
			} catch (TypeException exception) {
				System.out.println(
					"Type checking error in a declaration: " +
					exception.text
				);
			}
		return visit(node.exp);
	}

	@Override
	public TypeNode visitNode(ProgNode node) throws TypeException {
		if (this.print) {
			this.printNode(node);
		}
		return visit(node.exp);
	}

	@Override
	public TypeNode visitNode(FunNode node) throws TypeException {
		if (this.print) {
			this.printNode(node, node.id);
		}
		for (Node dec : node.decList)
			try {
				this.visit(dec);
			} catch (IncomplException exception) {
			} catch (TypeException exception) {
				System.out.println(
					"Type checking error in a declaration: " +
					exception.text
				);
			}
		if (!isSubtype(this.visit(node.exp), this.ckvisit(node.retType)))
			throw new TypeException(
				"Wrong return type for function " +
				node.id,node.getLine()
			);
		return null;
	}

	@Override
	public TypeNode visitNode(VarNode node) throws TypeException {
		if (this.print) {
			this.printNode(node, node.id);
		}
		if (!isSubtype(this.visit(node.exp), this.ckvisit(node.getType()))) {
			throw new TypeException(
				"Incompatible value for variable " +
				node.id, node.getLine()
			);
		}
		return null;
	}

	@Override
	public TypeNode visitNode(PrintNode node) throws TypeException {
		if (this.print) {
			this.printNode(node);
		}
		return this.visit(node.exp);
	}

	@Override
	public TypeNode visitNode(IfNode node) throws TypeException {
		if (this.print) {
			this.printNode(node);
		}
		if (!(isSubtype(this.visit(node.cond), new BoolTypeNode()))) {
			throw new TypeException(
				"Non boolean condition in if",
				node.getLine()
			);
		}
		TypeNode thenExp = this.visit(node.thenExp);
		TypeNode elseExp = this.visit(node.elseExp);
		if (isSubtype(thenExp, elseExp)) return elseExp;
		if (isSubtype(elseExp, thenExp)) return thenExp;
		throw new TypeException(
			"Incompatible types in then-else branches",
			node.getLine()
		);
	}

	@Override
	public TypeNode visitNode(EqualNode node) throws TypeException {
		if (this.print) {
			this.printNode(node);
		}
		TypeNode left = this.visit(node.left);
		TypeNode right = this.visit(node.right);
		if (!(isSubtype(left, right) || isSubtype(right, left))) {
			throw new TypeException(
				"Incompatible types in equal",
				node.getLine()
			);
		}
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(TimesNode node) throws TypeException {
		if (this.print) {
			this.printNode(node);
		}
		if (!(isSubtype(this.visit(node.left), new IntTypeNode())
				&& isSubtype(this.visit(node.right), new IntTypeNode()))) {
			throw new TypeException("Non integers in multiplication", node.getLine());
		}
		return new IntTypeNode();
	}

	@Override
	public TypeNode visitNode(PlusNode node) throws TypeException {
		if (this.print) {
			this.printNode(node);
		}
		if (!(isSubtype(this.visit(node.left), new IntTypeNode())
				&& isSubtype(this.visit(node.right), new IntTypeNode()))) {
			throw new TypeException("Non integers in sum", node.getLine());
		}
		return new IntTypeNode();
	}

	@Override
	public TypeNode visitNode(CallNode node) throws TypeException {
		if (this.print) {
			this.printNode(node, node.id);
		}
		TypeNode type = visit(node.entry);
		if (!(type instanceof ArrowTypeNode arrowType)) {
			throw new TypeException(
				"Invocation of a non-function " + node.id,
				node.getLine()
			);
		}
    if (!(arrowType.parList.size() == node.argList.size())) {
			throw new TypeException(
				"Wrong number of parameters in the invocation of " + node.id,
				node.getLine());
		}
		for (int i = 0; i < node.argList.size(); i++)
			if (
				!(isSubtype(this.visit(node.argList.get(i)), arrowType.parList.get(i)))
			) {
				throw new TypeException(
					"Wrong type for " + (i + 1) +
					"-th parameter in the invocation of " + node.id,
					node.getLine());
			}
		return arrowType.returnType;
	}

	@Override
	public TypeNode visitNode(IdNode node) throws TypeException {
		if (this.print) {
			this.printNode(node, node.id);
		}
		TypeNode type = this.visit(node.entry);
		if (type instanceof ArrowTypeNode)
			throw new TypeException(
				"Wrong usage of function identifier " + node.id,
				node.getLine()
			);
		if (type instanceof ClassTypeNode)
			throw new TypeException(
				"Wrong usage of class identifier " + node.id,
				node.getLine());
		return type;
	}

	@Override
	public TypeNode visitNode(BoolNode node) {
		if (this.print) {
			this.printNode(node, node.val.toString());
		}
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(IntNode node) {
		if (this.print) {
			this.printNode(node, node.val.toString());
		}
		return new IntTypeNode();
	}

	// gestione tipi incompleti	(se lo sono lancia eccezione)
	
	@Override
	public TypeNode visitNode(ArrowTypeNode node) throws TypeException {
		if (this.print) {
			this.printNode(node);
		}
		for (Node par: node.parList) {
			this.visit(par);
		}
		this.visit(node.returnType,"->"); //marks return type
		return null;
	}

	@Override
	public TypeNode visitNode(BoolTypeNode node) {
		if (this.print) {
			this.printNode(node);
		}
		return null;
	}

	@Override
	public TypeNode visitNode(IntTypeNode node) {
		if (this.print) {
			this.printNode(node);
		}
		return null;
	}

// STentry (ritorna campo type)

	@Override
	public TypeNode visitSTentry(STentry entry) throws TypeException {
		if (this.print) {
			this.printSTentry("type");
		}
		return this.ckvisit(entry.type);
	}

	@Override
	public TypeNode visitNode(GreaterEqualNode node) throws TypeException {
		if (this.print) {
			this.printNode(node);
		}
		TypeNode left = this.visit(node.left);
		TypeNode right = this.visit(node.right);
		if (!(isSubtype(left, right) || isSubtype(right, left)))
			throw new TypeException(
				"Incompatible types in GreaterEqual",
				node.getLine()
			);
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(LessEqualNode node) throws TypeException {
		if (this.print) {
			this.printNode(node);
		}
		TypeNode left = this.visit(node.left);
		TypeNode right = this.visit(node.right);
		if (!(isSubtype(left, right) || isSubtype(right, left)))
			throw new TypeException(
				"Incompatible types in LessEqual",
				node.getLine()
			);
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(NotNode node) throws TypeException {
		if (this.print) {
			this.printNode(node);
		}
		TypeNode expression = visit(node.expression);
		if (!(isSubtype(expression, new BoolTypeNode())))
			throw new TypeException(
				"Non boolean expression in Not",
				node.getLine()
			);
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(MinusNode node) throws TypeException {
		if (this.print) {
			this.printNode(node);
		}
		TypeNode left = this.visit(node.left);
		TypeNode right = this.visit(node.right);
		if (
			!(isSubtype(left, new IntTypeNode())
			&& isSubtype(right, new IntTypeNode()))
		) {
			throw new TypeException("Non Integers in Minus", node.getLine());
		}
		return new IntTypeNode();
	}

	@Override
	public TypeNode visitNode(OrNode node) throws TypeException {
		if (this.print) {
			this.printNode(node);
		}
		TypeNode left = visit(node.left);
		TypeNode right = visit(node.right);
		if (
			!(isSubtype(left, new BoolTypeNode())
			&& isSubtype(right, new BoolTypeNode()))
		)
			throw new TypeException("Non Booleans in Or", node.getLine());
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(DivNode node) throws TypeException {
		if (this.print) {
			this.printNode(node);
		}
		TypeNode left = this.visit(node.left);
		TypeNode right = this.visit(node.right);
		if (
			!(isSubtype(left, new IntTypeNode())
			&& isSubtype(right, new IntTypeNode()))
		)
			throw new TypeException("Non Integer in Div", node.getLine());
		return new IntTypeNode();
	}

	@Override
	public TypeNode visitNode(AndNode node) throws TypeException {
		if (this.print) {
			this.printNode(node);
		}
		TypeNode left = this.visit(node.left);
		TypeNode right = this.visit(node.right);
		if (
			!(isSubtype(left, new BoolTypeNode())
			&& isSubtype(right, new BoolTypeNode()))
		)
			throw new TypeException("Non Booleans in And", node.getLine());
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(ClassNode node) throws TypeException {
		if (this.print) {
			this.printNode(node);
		}

		List<TypeNode> fields = new ArrayList<>();
		for (FieldNode field : node.fields) {
			fields.add(field.getType());
		}

		List<TypeNode> methods = new ArrayList<>();
		for (MethodNode method : node.methods) {
			try {
				this.visit(method);
				methods.add(method.getType());
			} catch (IncomplException e) {
			} catch (TypeException e) {
				System.out.println(
					"Type checking error in a declaration: " + e.text
				);
			}
		}
		return new ClassTypeNode(fields, methods);
	}

	@Override
	public TypeNode visitNode(MethodNode node) throws TypeException {
		if (this.print) {
			printNode(node);
		}

		for (Node dec : node.decList) {
			try {
				this.visit(dec);
			} catch (IncomplException e) {
			} catch (TypeException e) {
				System.out.println(
					"Type checking error in a declaration: " + e.text
				);
			}
		}

		if (
			!isSubtype(
				this.visit(node.exp),
				this.ckvisit(node.retType)
			)
		) {
			throw new TypeException(
				"Wrong return type for function " + node.name,
				node.getLine()
			);
		}
		return null;
	}

	@Override
	public TypeNode visitNode(ClassCallNode node) throws TypeException {
		if (this.print) {
			this.printNode(node);
		}

		TypeNode type = this.visit(node.methodEntry);
		if (!(type instanceof ArrowTypeNode)) {
			throw new TypeException(
				"Invocation of a non-function " + node.objId,
				node.getLine());
		}

		ArrowTypeNode arrowType = (ArrowTypeNode) type;
		if (!(arrowType.parList.size() == node.argList.size())) {
			throw new TypeException(
				"Wrong number of parameters in the invocation of "
					+ node.methodId,
				node.getLine()
			);
		}

		for (int i = 0; i < node.argList.size(); i++) {
			if (!(isSubtype(
				visit(node.argList.get(i)),
				arrowType.parList.get(i)
			))) {
				throw new TypeException(
					"Wrong type for " +
						(i + 1) +
						"-th parameter in the invocation of " +
						node.methodId,
					node.getLine());
			}
		}
		return arrowType.returnType;
	}

	@Override
	public TypeNode visitNode(NewNode node) throws TypeException {
		if (this.print) {
			this.printNode(node);
		}

		TypeNode type = this.visit(node.entry);
		if (!(type instanceof ClassTypeNode)) {
			throw new TypeException(
				"Invocation of a non-class " + node.classId,
				node.getLine());
		}

		ClassTypeNode classType = (ClassTypeNode) type;
		if (!(classType.allFields.size() == node.argList.size())) {
			throw new TypeException(
				"Wrong number of parameters in the creation of an object of class "
					+ node.classId,
				node.getLine()
			);
		}

		for (int i = 0; i < node.argList.size(); i++) {
			if (!(isSubtype(
				visit(node.argList.get(i)),
				classType.allFields.get(i)
			))) {
				throw new TypeException(
					"Wrong type for " +
						(i + 1) +
						"-th field in the creation of an object of class " +
						node.classId,
					node.getLine());
			}
		}
		return new RefTypeNode(node.classId);
	}

	@Override
	public TypeNode visitNode(EmptyNode node) throws TypeException {
		if (this.print) {
			this.printNode(node);
		}

		return new EmptyTypeNode();
	}

	@Override
	public TypeNode visitNode(ClassTypeNode n) throws TypeException {
		return null;
	}

	@Override
	public TypeNode visitNode(RefTypeNode n) throws TypeException {
		return null;
	}

	@Override
	public TypeNode visitNode(EmptyTypeNode n) throws TypeException {
		return null;
	}
}