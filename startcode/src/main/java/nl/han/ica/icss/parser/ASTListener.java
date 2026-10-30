package nl.han.ica.icss.parser;

import nl.han.ica.datastructures.HANStack;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {
	
	//Accumulator attributes:
	private AST ast;

	//Use this to keep track of the parent nodes when recursively traversing the ast
	private IHANStack<ASTNode> currentContainer;

	public ASTListener() {
		ast = new AST();
		currentContainer = new HANStack<>();
	}
    public AST getAST() {
        return ast;
    }

	@Override
	public void enterStylesheet(ICSSParser.StylesheetContext contextobject) {
		currentContainer.push(ast.root); // zet een stylesheet op de stack, root bevat lege stylesheet
	}

	@Override
	public void exitStylesheet(ICSSParser.StylesheetContext contextobject) {
		currentContainer.pop(); // haal de stylesheet van de stack af
	}

	@Override
	public void enterStylerule(ICSSParser.StyleruleContext contextobject) {
		currentContainer.push(new Stylerule());
	}

	@Override
	public void exitStylerule(ICSSParser.StyleruleContext contextobject) {
		ASTNode stylerule = currentContainer.pop();
		currentContainer.peek().addChild(stylerule); // voegt de stylerule toe aan de stylesheet
	}

	@Override
	public void exitSelector(ICSSParser.SelectorContext contextobject) {
		ASTNode selector;

		if (contextobject.ID_IDENT() != null) {
			selector = new IdSelector(contextobject.getText());
		} else if (contextobject.CLASS_IDENT() != null) {
			selector = new ClassSelector(contextobject.getText());
		} else { // LOWER_IDENT
			selector = new TagSelector(contextobject.getText());
		}

		currentContainer.peek().addChild(selector); // voegt de selector toe aan de stylerule
	}

	@Override
	public void enterDeclaration(ICSSParser.DeclarationContext contextobject) {
		currentContainer.push(new Declaration());
	}

	@Override
	public void exitDeclaration(ICSSParser.DeclarationContext contextobject) {
		ASTNode declaration = currentContainer.pop();
		currentContainer.peek().addChild(declaration); // voegt de declaration toe aan de stylerule
	}

	@Override
	public void exitProperty(ICSSParser.PropertyContext contextobject) {
		currentContainer.peek().addChild( // voegt de property toe aan de declaration
				new PropertyName(contextobject.getText())
		);
	}

	@Override
	public void exitLiteral(ICSSParser.LiteralContext contextobject) {
		ASTNode literal;

		if (contextobject.PIXELSIZE() != null) { // kijk of pixel is
			literal = new PixelLiteral(contextobject.getText());
		} else if (contextobject.PERCENTAGE() != null) { // kijk of percentage is
			literal = new PercentageLiteral(contextobject.getText());
		} else if (contextobject.SCALAR() != null) { // kijk of scalar is
			literal = new ScalarLiteral(contextobject.getText());
		} else if (contextobject.COLOR() != null) { // kijk of kleur is
			literal = new ColorLiteral(contextobject.getText());
		} else { // anders boolean
			literal = new BoolLiteral(contextobject.getText());
		}

		currentContainer.peek().addChild(literal);
	}

	@Override
	public void enterVariableAssignment(
			ICSSParser.VariableAssignmentContext contextobject) {

		currentContainer.push(new VariableAssignment());
	}

	@Override
	public void exitVariableAssignment(
			ICSSParser.VariableAssignmentContext contextobject) {

		ASTNode assignment = currentContainer.pop();
		currentContainer.peek().addChild(assignment);
	}

	@Override
	public void exitVariableReference(
			ICSSParser.VariableReferenceContext contextobject) {

		currentContainer.peek().addChild( // voegt de variableReference toe
				new VariableReference(contextobject.getText())
		);
	}

	@Override
	public void enterMultiplyExpression(
			ICSSParser.MultiplyExpressionContext contextobject) {

		currentContainer.push(new MultiplyOperation());
	}

	@Override
	public void exitMultiplyExpression(
			ICSSParser.MultiplyExpressionContext contextobject) {

		ASTNode operation = currentContainer.pop();
		currentContainer.peek().addChild(operation);
	}

	@Override
	public void enterAddSubtractExpression(
			ICSSParser.AddSubtractExpressionContext contextobject) {

		if (contextobject.op.getType() == ICSSParser.PLUS) {
			currentContainer.push(new AddOperation());
		} else { // min
			currentContainer.push(new SubtractOperation());
		}
	}

	@Override
	public void exitAddSubtractExpression(
			ICSSParser.AddSubtractExpressionContext contextobject) {

		ASTNode operation = currentContainer.pop();
		currentContainer.peek().addChild(operation);
	}

	@Override
	public void enterIfClause(ICSSParser.IfClauseContext contextobject) {
		currentContainer.push(new IfClause());
	}

	@Override
	public void exitIfClause(ICSSParser.IfClauseContext contextobject) {
		ASTNode ifClause = currentContainer.pop();
		currentContainer.peek().addChild(ifClause);
	}

	@Override
	public void enterElseClause(ICSSParser.ElseClauseContext contextobject) {
		currentContainer.push(new ElseClause());
	}

	@Override
	public void exitElseClause(ICSSParser.ElseClauseContext contextobject) {
		ASTNode elseClause = currentContainer.pop();
		currentContainer.peek().addChild(elseClause);
	}
}