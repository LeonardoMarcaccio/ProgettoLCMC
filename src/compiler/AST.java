package compiler;

import java.util.*;

import compiler.lib.*;

/**
 * Class that implements all the possible nodes that compose the Abstract Syntax Tree
 */
public class AST {

	/**
	 * Program with Declarations Node
	 */
	public static class ProgLetInNode extends Node {
		final List<DecNode> decList;
		final Node exp;
		ProgLetInNode(
			List<DecNode> decList,
			Node exp) {
			this.decList = Collections.unmodifiableList(decList);
			this.exp = exp;
		}

		@Override
		public <S,E extends Exception> S accept(
			BaseASTVisitor<S,E> visitor
		) throws E {
			return visitor.visitNode(this);
		}
	}

	/**
	 * Program without Declarations Node
	 */
	public static class ProgNode extends Node {
		final Node exp;
		ProgNode(Node e) {exp = e;}

		@Override
		public <S,E extends Exception> S accept(
			BaseASTVisitor<S,E> visitor
		) throws E {
			return visitor.visitNode(this);
		}
	}

	/**
	 * Function Node
	 */
	public static class FunNode extends DecNode {
		final String id;
		final TypeNode retType;
		final List<ParNode> parList;
		final List<DecNode> decList;
		final Node exp;

		public FunNode(
			String id,
			TypeNode retType,
			List<ParNode> parList,
			List<DecNode> decList,
			Node exp) {
			this.id = id;
			this.retType = retType;
			this.parList = parList;
			this.decList = decList;
			this.exp = exp;
		}

		@Override
		public <S,E extends Exception> S accept(
			BaseASTVisitor<S,E> visitor
		) throws E {
			return visitor.visitNode(this);
		}
	}

	/**
	 * Function Parameter Node
	 */
	public static class ParNode extends DecNode {
		final String id;
		ParNode(String id, TypeNode type) {
			this.id = id;
			this.type = type;
		}

		@Override
		public <S,E extends Exception> S accept(
			BaseASTVisitor<S,E> visitor
		) throws E {
			return visitor.visitNode(this);
		}
	}

	/**
	 * Variable Node
	 */
	public static class VarNode extends DecNode {
		final String id;
		final Node exp;

		VarNode(String id, TypeNode type, Node exp) {
			this.id = id;
			this.type = type;
			this.exp = exp;
		}

		@Override
		public <S,E extends Exception> S accept(
			BaseASTVisitor<S,E> visitor
		) throws E {
			return visitor.visitNode(this);
		}
	}

	/**
	 * Screen Output Node
	 */
	public static class PrintNode extends Node {
		final Node exp;

		PrintNode(Node exp) {
			this.exp = exp;
		}

		@Override
		public <S,E extends Exception> S accept(
			BaseASTVisitor<S,E> visitor
		) throws E {
			return visitor.visitNode(this);
		}
	}

	/**
	 * If-Then-Else Statement Node
	 */
	public static class IfNode extends Node {
		final Node cond;
		final Node thenExp;
		final Node elseExp;

		IfNode(
			Node cond,
			Node thenExp,
			Node elseExp
		) {
			this.cond = cond;
			this.thenExp = thenExp;
			this.elseExp = elseExp;
		}

		@Override
		public <S,E extends Exception> S accept(
			BaseASTVisitor<S,E> visitor
		) throws E {
			return visitor.visitNode(this);
		}
	}

	/**
	 * Equal Evaluation Node
	 */
	public static class EqualNode extends Node {
		final Node left;
		final Node right;

		EqualNode(Node left, Node right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public <S,E extends Exception> S accept(
			BaseASTVisitor<S,E> visitor
		) throws E {
			return visitor.visitNode(this);
		}
	}

	/**
	 * Multiplication Node
	 */
	public static class TimesNode extends Node {
		final Node left;
		final Node right;

		TimesNode(Node left, Node right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public <S,E extends Exception> S accept(
			BaseASTVisitor<S,E> visitor
		) throws E {
			return visitor.visitNode(this);
		}
	}

	/**
	 * Sum Node
	 */
	public static class PlusNode extends Node {
		final Node left;
		final Node right;

		PlusNode(Node left, Node right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public <S,E extends Exception> S accept(
			BaseASTVisitor<S,E> visitor
		) throws E {
			return visitor.visitNode(this);
		}
	}

	/**
	 * Call a specific Function Node
	 */
	public static class CallNode extends Node {
		final String id;
		final List<Node> argList;
		STentry entry;
		int nestingLevel;

		CallNode(String id, List<Node> argList) {
			this.id = id;
			this.argList = Collections.unmodifiableList(argList);
		}

		@Override
		public <S,E extends Exception> S accept(
			BaseASTVisitor<S,E> visitor
		) throws E {
			return visitor.visitNode(this);
		}
	}

	/**
	 * Call a Variable Node
	 */
	public static class IdNode extends Node {
		final String id;
		STentry entry;
		int nestingLevel;

		IdNode(String id) {
			this.id = id;
		}

		@Override
		public <S,E extends Exception> S accept(
			BaseASTVisitor<S,E> visitor
		) throws E {
			return visitor.visitNode(this);
		}
	}

	/**
	 * Boolean Type Value Node
	 */
	public static class BoolNode extends Node {
		final Boolean val;

		BoolNode(boolean val) {
			this.val = val;
		}

		@Override
		public <S,E extends Exception> S accept(
			BaseASTVisitor<S,E> visitor
		) throws E {
			return visitor.visitNode(this);
		}
	}

	/**
	 * Integer Type Value Node
	 */
	public static class IntNode extends Node {
		final Integer val;

		IntNode(Integer val) {
			this.val = val;
		}

		@Override
		public <S,E extends Exception> S accept(
			BaseASTVisitor<S,E> visitor
		) throws E {
			return visitor.visitNode(this);
		}
	}
	
	public static class ArrowTypeNode extends TypeNode {
		final List<TypeNode> parList;
		final TypeNode returnType;

		ArrowTypeNode(
			List<TypeNode> parList,
			TypeNode returnType
		) {
			this.parList = Collections.unmodifiableList(parList);
			this.returnType = returnType;
		}

		@Override
		public <S,E extends Exception> S accept(
			BaseASTVisitor<S,E> visitor
		) throws E {
			return visitor.visitNode(this);
		}
	}

	/**
	 * Bool Type Node
	 */
	public static class BoolTypeNode extends TypeNode {
		@Override
		public <S,E extends Exception> S accept(
			BaseASTVisitor<S,E> visitor
		) throws E {
			return visitor.visitNode(this);
		}
	}

	/**
	 * Integer Type Node
	 */
	public static class IntTypeNode extends TypeNode {
		@Override
		public <S,E extends Exception> S accept(
			BaseASTVisitor<S,E> visitor
		) throws E {
			return visitor.visitNode(this);
		}
	}

	/* ------------------ Extra Methods ------------------ */

	/**
	 * >= Operator Node
	 */
	public static class GreaterEqualNode extends Node {
		final Node left;
		final Node right;

		GreaterEqualNode(Node left, Node right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public <S,E extends Exception> S accept(
			BaseASTVisitor<S,E> visitor
		) throws E {
			return visitor.visitNode(this);
		}
	}

	/**
	 * <= Operator Node
	 */
	public static class LessEqualNode extends Node {
		final Node left;
		final Node right;

		LessEqualNode(Node left, Node right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public <S,E extends Exception> S accept(
			BaseASTVisitor<S,E> visitor
		) throws E {
			return visitor.visitNode(this);
		}
	}

	/**
	 * Negate Operator Node
	 */
	public static class NotNode extends Node {
		final Node expression;

		NotNode(Node expression) {
			this.expression = expression;
		}

		@Override
		public <S,E extends Exception> S accept(
			BaseASTVisitor<S,E> visitor
		) throws E {
			return visitor.visitNode(this);
		}
	}

	/**
	 * Minus Operation or Negative Integer Node
	 */
	public static class MinusNode extends Node {
		final Node left;
		final Node right;

		MinusNode(Node left, Node right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public <S,E extends Exception> S accept(
			BaseASTVisitor<S,E> visitor
		) throws E {
			return visitor.visitNode(this);
		}
	}

	/**
	 * Or Logical Operation Node
	 */
	public static class OrNode extends Node {
		final Node left;
		final Node right;

		OrNode(Node left, Node right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public <S,E extends Exception> S accept(
			BaseASTVisitor<S,E> visitor
		) throws E {
			return visitor.visitNode(this);
		}
	}

	/**
	 * Division Operation Node
	 */
	public static class DivNode extends Node {
		final Node left;
		final Node right;

		DivNode(Node left, Node right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public <S,E extends Exception> S accept(
			BaseASTVisitor<S,E> visitor
		) throws E {
			return visitor.visitNode(this);
		}
	}

	/**
	 * And Logic Operation Node
	 */
	public static class AndNode extends Node {
		final Node left;
		final Node right;

		AndNode(Node left, Node right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public <S,E extends Exception> S accept(
			BaseASTVisitor<S,E> visitor
		) throws E {
			return visitor.visitNode(this);
		}
	}

	/* ------------------ Object Oriented ------------------ */

	/**
	 * Class Node
	 */
	public static class ClassNode extends DecNode {
		final String className;
		final List<FieldNode> fields;
		final List<MethodNode> methods;

		ClassNode(
			String className,
			List<FieldNode> fields,
			List<MethodNode> methods
		) {
			this.className = className;
			this.fields = Collections.unmodifiableList(fields);
			this.methods = Collections.unmodifiableList(methods);
		}

		//void setType(TypeNode t) {type = t;}

		@Override
		public <S,E extends Exception> S accept(
			BaseASTVisitor<S,E> visitor
		) throws E {
			return visitor.visitNode(this);
		}
	}

	/**
	 * Class Field Node
	 */
	public static class FieldNode extends DecNode {
		final String name;
		int offset;

		FieldNode(String name, TypeNode type) {
			this.name = name;
			this.type = type;
		}

		@Override
		public <S,E extends Exception> S accept(
			BaseASTVisitor<S,E> visitor
		) throws E {
			return visitor.visitNode(this);
		}
	}

	/**
	 * Class Method Node
	 */
	public static class MethodNode extends DecNode {
		final String name;
		final TypeNode retType;
		final List<ParNode> parList;
		final List<DecNode> decList;
		final Node exp;
		String label;
		int offset;

		MethodNode(
			String name,
			TypeNode retType,
			List<ParNode> parList,
			List<DecNode> decList,
			Node exp
		) {
			this.name = name;
			this.retType = retType;
			this.parList = Collections.unmodifiableList(parList);
			this.decList = Collections.unmodifiableList(decList);
			this.exp = exp;
		}

		@Override
		public <S,E extends Exception> S accept(
			BaseASTVisitor<S,E> visitor
		) throws E {
			return visitor.visitNode(this);
		}
	}

	/**
	 * Class.Method Call Node
	 */
	public static class ClassCallNode extends Node {
		final String objId;
		final String methodId;
		final List<Node> argList;
		STentry entry;
		STentry methodEntry;
		int nestingLevel;

		ClassCallNode(
			String objId,
			String methodId,
			List<Node> argList
		) {
			this.objId = objId;
			this.methodId = methodId;
			this.argList = Collections.unmodifiableList(argList);
		}

		@Override
		public <S,E extends Exception> S accept(
			BaseASTVisitor<S,E> visitor
		) throws E {
			return visitor.visitNode(this);
		}
	}

	/**
	 * New Class Caller Node
	 */
	public static class NewNode extends Node {
		final String classId;
		final List<Node> argList;
		STentry entry;

		NewNode(String classId, List<Node> argList) {
			this.classId = classId;
			this.argList = Collections.unmodifiableList(argList);
		}

		@Override
		public <S,E extends Exception> S accept(
			BaseASTVisitor<S,E> visitor
		) throws E {
			return visitor.visitNode(this);
		}
	}

	/**
	 * Null Node
	 */
	public static class EmptyNode extends Node {
		EmptyNode() {}

		@Override
		public <S,E extends Exception> S accept(
			BaseASTVisitor<S,E> visitor
		) throws E {
			return visitor.visitNode(this);
		}
	}

	/**
	 * Class Type Node
	 */
	public static class ClassTypeNode extends TypeNode {
		final List<TypeNode> allFields;
		final List<TypeNode> allMethods;

    public ClassTypeNode(List<TypeNode> allFields, List<TypeNode> allMethods) {
      this.allFields = allFields;
      this.allMethods = allMethods;
    }

    @Override
		public <S,E extends Exception> S accept(
			BaseASTVisitor<S,E> visitor
		) throws E {
			return visitor.visitNode(this);
		}
	}

	/**
	 * Node to get the Type Reference for a Class
	 */
	public static class RefTypeNode extends TypeNode {
		final String id; // The actual class name

		public RefTypeNode(String id) {
			this.id = id;
		}

		@Override
		public <S,E extends Exception> S accept(
			BaseASTVisitor<S,E> visitor
		) throws E {
			return visitor.visitNode(this);
		}
	}

	/**
	 * Null Type Node
	 */
	public static class EmptyTypeNode extends TypeNode {
		public EmptyTypeNode() {}

		@Override
		public <S,E extends Exception> S accept(
			BaseASTVisitor<S,E> visitor
		) throws E {
			return visitor.visitNode(this);
		}
	}
}