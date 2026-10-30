package nl.han.ica.icss.generator;

import nl.han.ica.icss.ast.AST;
import nl.han.ica.icss.ast.ASTNode;
import nl.han.ica.icss.ast.Declaration;
import nl.han.ica.icss.ast.Expression;
import nl.han.ica.icss.ast.Stylerule;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;

public class Generator {

	public String generate(AST ast) {
		StringBuilder css = new StringBuilder();

		for (ASTNode node : ast.root.body) {
			if (!(node instanceof Stylerule)) {
				throw new IllegalStateException(
						"De stylesheet moet eerst worden getransformeerd."
				);
			}

			// lege regel tussen opmaakregels
			if (css.length() > 0) {
				css.append("\n");
			}

			generateStylerule((Stylerule) node, css, 0);
		}

		return css.toString();

	}

	private void generateStylerule(
			Stylerule stylerule,
			StringBuilder css,
			int indentationLevel) {

		if (stylerule.selectors.size() != 1) {
			throw new IllegalStateException(
					"Een opmaakregel moet precies één selector hebben."
			);
		}

		appendIndentation(css, indentationLevel);

		css.append(stylerule.selectors.get(0).toString());
		css.append(" {\n");

		for (ASTNode node : stylerule.body) {
			if (!(node instanceof Declaration)) {
				throw new IllegalStateException(
						"Een getransformeerde opmaakregel "
								+ "mag alleen declaraties bevatten."
				);
			}

			generateDeclaration(
					(Declaration) node,
					css,
					indentationLevel + 1
			);
		}

		appendIndentation(css, indentationLevel);
		css.append("}\n");
	}

	private void generateDeclaration(
			Declaration declaration,
			StringBuilder css,
			int indentationLevel) {

		appendIndentation(css, indentationLevel);

		css.append(declaration.property.name);
		css.append(": ");
		css.append(generateValue(declaration.expression));
		css.append(";\n");
	}

	private String generateValue(Expression expression) {
		if (expression instanceof PixelLiteral) {
			return ((PixelLiteral) expression).value + "px";
		}

		if (expression instanceof PercentageLiteral) {
			return ((PercentageLiteral) expression).value + "%";
		}

		if (expression instanceof ColorLiteral) {

			return ((ColorLiteral) expression).value; // # zit al in waarde
		}

		throw new IllegalStateException(
				"Een CSS-declaratie moet een concrete kleur, "
						+ "pixelwaarde of percentage bevatten."
		);
	}

	private void appendIndentation(
			StringBuilder css,
			int indentationLevel) {

		css.append("  ".repeat(indentationLevel));
	}
	
}
