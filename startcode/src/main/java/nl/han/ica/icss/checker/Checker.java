package nl.han.ica.icss.checker;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Checker {

    // Iedere map bevat de variabelen van een scope, laatste is lokaal
    private final List<Map<String, ExpressionType>> variableTypes = // in de map zit bijvoorbeeld MainWitth -> PIXEL
            new ArrayList<>();

    public void check(AST ast) {
        variableTypes.clear(); // voor als er nog variableTypes van de vorige inzaten
        checkBody(ast.root.body);
    }

    private void checkBody(List<ASTNode> body) {

        variableTypes.add(new HashMap<>()); // lokale scope body

        for (ASTNode node : body) { // voor elke node in de body, volgorde belangrijk begin vooraan
            checkNode(node);
        }

        variableTypes.remove(variableTypes.size() - 1); // lokale scope body verwijderen
    }

    private void checkNode(ASTNode node) {
        if (node instanceof VariableAssignment) {
            checkAssignment((VariableAssignment) node);
        } else if (node instanceof Declaration) {
            checkDeclaration((Declaration) node);
        } else if (node instanceof Stylerule) {
            checkBody(((Stylerule) node).body);
        } else if (node instanceof IfClause) {
            checkIfClause((IfClause) node);
        }
    }

    private void checkAssignment(VariableAssignment assignment) {
        // Dit eerst doen om te voorkomen dat als variabele in zijn eigen definitie wordt gebruikt dit wordt goedgekeurd doordat hij eerst wordt toegevoegd
        ExpressionType type = getExpressionType(assignment.expression); // pak type van assignment

        Map<String, ExpressionType> currentScope =
                variableTypes.get(variableTypes.size() - 1); // pak laatste scope

        currentScope.put(assignment.name.name, type); // sla altijd op in huidige scope
    }

    private ExpressionType getVariableType(VariableReference reference) {

        for (int i = variableTypes.size() - 1; i >= 0; i--) { // begin lokaal en ga steeds 1 stap hoger
            Map<String, ExpressionType> scope = variableTypes.get(i); // pak scope

            if (scope.containsKey(reference.name)) {
                return scope.get(reference.name); // gevonden
            }
        }

        // niet gevonden, geef error
        reference.setError(
                "Variabele '" + reference.name
                        + "' is niet gedefinieerd in deze scope."
        );

        return ExpressionType.UNDEFINED;
    }

    private void checkDeclaration(Declaration declaration) {
        ExpressionType type = getExpressionType(declaration.expression);
        String property = declaration.property.name;

        switch (property) {
            case "color":
            case "background-color":
                if (type != ExpressionType.UNDEFINED
                        && type != ExpressionType.COLOR) {

                    declaration.setError(
                            "'" + property + "' verwacht een kleur, maar krijgt "
                                    + type + "."
                    );
                }
                break;

            case "width":
            case "height":
                if (type != ExpressionType.UNDEFINED
                        && type != ExpressionType.PIXEL
                        && type != ExpressionType.PERCENTAGE) {

                    declaration.setError(
                            "'" + property
                                    + "' verwacht pixels of een percentage, maar krijgt "
                                    + type + "."
                    );
                }
                break;

            default: // property is niet color, background-color, width of height
                declaration.property.setError(
                        "Onbekende property '" + property + "'."
                );
                break;
        }
    }

    private void checkIfClause(IfClause ifClause) {
        ExpressionType conditionType =
                getExpressionType(ifClause.conditionalExpression);

        if (conditionType != ExpressionType.BOOL) {
            ifClause.setError(
                    "De conditie van een if moet boolean zijn, maar krijgt "
                            + conditionType + "."
            );
        }

        checkBody(ifClause.body);

        if (ifClause.elseClause != null) {
            checkBody(ifClause.elseClause.body);
        }
    }

    private ExpressionType getExpressionType(Expression expression) {
        if (expression instanceof PixelLiteral) {
            return ExpressionType.PIXEL;
        }

        if (expression instanceof PercentageLiteral) {
            return ExpressionType.PERCENTAGE;
        }

        if (expression instanceof ScalarLiteral) {
            return ExpressionType.SCALAR;
        }

        if (expression instanceof ColorLiteral) {
            return ExpressionType.COLOR;
        }

        if (expression instanceof BoolLiteral) {
            return ExpressionType.BOOL;
        }

        if (expression instanceof VariableReference) {
            return getVariableType((VariableReference) expression);
        }

        if (expression instanceof Operation) {
            return getOperationType((Operation) expression);
        }

        return ExpressionType.UNDEFINED;
    }

    private ExpressionType getOperationType(Operation operation) {

        ExpressionType leftType = getExpressionType(operation.lhs); // linkerhelft expressie
        ExpressionType rightType = getExpressionType(operation.rhs); // rechterhelft expressie

        if (leftType == ExpressionType.COLOR
                || rightType == ExpressionType.COLOR) { // check bevat kleur

            operation.setError(
                    "Kleuren mogen niet worden gebruikt in berekeningen."
            );

            return ExpressionType.UNDEFINED;
        }

        if (leftType == ExpressionType.UNDEFINED
                || rightType == ExpressionType.UNDEFINED) {

            return ExpressionType.UNDEFINED;
        }

        if (operation instanceof AddOperation
                || operation instanceof SubtractOperation) {

            if (leftType == rightType && isNumeric(leftType)) { // als links en rechts zelfde type en numeric dan kun je ze optellen of aftrekken dus is het goed
                return leftType;
            }

            return ExpressionType.UNDEFINED;
        }

        if (operation instanceof MultiplyOperation) {
            if (leftType == ExpressionType.SCALAR && isNumeric(rightType)) {
                return rightType; // 2 * 2px blijft px
            }

            if (rightType == ExpressionType.SCALAR && isNumeric(leftType)) {
                return leftType;
            }

            return ExpressionType.UNDEFINED;
        }

        return ExpressionType.UNDEFINED;
    }

    private boolean isNumeric(ExpressionType type) {
        return type == ExpressionType.PIXEL
                || type == ExpressionType.PERCENTAGE
                || type == ExpressionType.SCALAR;
    } // als pixel, percentage of scalar dan is het numeric
}
