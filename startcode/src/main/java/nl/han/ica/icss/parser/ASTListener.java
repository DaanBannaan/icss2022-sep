package nl.han.ica.icss.parser;

import nl.han.ica.datastructures.HANStack;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;

import nl.han.ica.icss.ast.AST;
import nl.han.ica.icss.ast.ASTNode;
import nl.han.ica.icss.ast.Declaration;
import nl.han.ica.icss.ast.PropertyName;
import nl.han.ica.icss.ast.Stylerule;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
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
		} else { // anders kleur
			literal = new ColorLiteral(contextobject.getText());
		}

		currentContainer.peek().addChild(literal); // voegt de literal toe aan de declaration
	}
}