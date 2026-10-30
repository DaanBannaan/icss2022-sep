package nl.han.ica.icss.transforms;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;

import java.util.*;

public class Evaluator implements Transform {

    private final List<Map<String, Literal>> variableValues =
            new ArrayList<>(); // zoals in checker maar nu met waardes in plaats van types

    @Override
    public void apply(AST ast) {
        if (!ast.getErrors().isEmpty()) {
            throw new IllegalStateException(
                    "Een AST met fouten mag niet worden getransformeerd."
            );
        }

        variableValues.clear();
        ast.root.body = transformBody(ast.root.body);
    }

    private ArrayList<ASTNode> transformBody(List<ASTNode> body) {
        variableValues.add(new HashMap<>());

        ArrayList<ASTNode> result = new ArrayList<>();

        try {

            for (ASTNode node : body) {
                if (node instanceof VariableAssignment) {
                    evaluateAssignment((VariableAssignment) node); // bereken variabelewaarde en zet in scope

                } else if (node instanceof Declaration) {
                    Declaration declaration = (Declaration) node;

                    declaration.expression =
                            evaluateExpression(declaration.expression);

                    result.add(declaration);
                } else if (node instanceof Stylerule) {
                    Stylerule stylerule = (Stylerule) node;

                    stylerule.body = transformBody(stylerule.body);

                    result.add(stylerule);
                } else if (node instanceof IfClause) {
                    result.addAll(transformIfClause((IfClause) node));
                } else {
                    throw new IllegalArgumentException(
                            "Onverwachte AST-node: "
                                    + node.getClass().getSimpleName()
                    );
                }
            }

            return result;
        } finally {

            variableValues.remove(variableValues.size() - 1); // verwijder lokale scope altijd
        }
    }

    private void evaluateAssignment(VariableAssignment assignment) {

        Literal value = evaluateExpression(assignment.expression); // bepaal waarde

        Map<String, Literal> currentScope =
                variableValues.get(variableValues.size() - 1);

        currentScope.put(assignment.name.name, value); // voeg variabelewaarde toe of vervang oude in scope
    }

    private Literal getVariableValue(VariableReference reference) {

        for (int i = variableValues.size() - 1; i >= 0; i--) { // doorloop alle scopes van lokaal naar globaal
            Map<String, Literal> scope = variableValues.get(i);

            if (scope.containsKey(reference.name)) {
                return scope.get(reference.name); // variabele gevonden
            }
        }

        throw new IllegalStateException(
                "Geen waarde gevonden voor variabele '"
                        + reference.name + "'."
        );
    }

    private Literal evaluateExpression(Expression expression) {
        if (expression instanceof Literal) {
            return (Literal) expression; // als al waarde is dan return waarde, stop recursie
        }

        if (expression instanceof VariableReference) {
            return getVariableValue((VariableReference) expression);
        }

        if (expression instanceof Operation) {
            return evaluateOperation((Operation) expression);
        }

        throw new IllegalArgumentException(
                "Onbekend expressietype: "
                        + expression.getClass().getSimpleName()
        );
    }

    private ArrayList<ASTNode> transformIfClause(IfClause ifClause) {
        Literal condition =
                evaluateExpression(ifClause.conditionalExpression);

        if (!(condition instanceof BoolLiteral)) {
            throw new IllegalStateException(
                    "De conditie van een if moet een boolean zijn."
            );
        }

        if (((BoolLiteral) condition).value) { // if TRUE
            return transformBody(ifClause.body);
        }

        if (ifClause.elseClause != null) { // if FALSE en else bestaat
            return transformBody(ifClause.elseClause.body);
        }

        // FALSE zonder else
        return new ArrayList<>();
    }

    private Literal evaluateOperation(Operation operation) {
        // eerst apart uitrekenen met recursie
        Literal left = evaluateExpression(operation.lhs);
        Literal right = evaluateExpression(operation.rhs);

        if (!isNumeric(left) || !isNumeric(right)) {
            throw new IllegalArgumentException(
                    "Berekeningen vereisen numerieke waarden."
            );
        }

        int leftValue = getNumericValue(left);
        int rightValue = getNumericValue(right);

        if (operation instanceof AddOperation
                || operation instanceof SubtractOperation) {

            if (left.getClass() != right.getClass()) {
                throw new IllegalArgumentException(
                        "Optellen en aftrekken vereisen gelijke types."
                );
            }

            int result;

            if (operation instanceof AddOperation) {
                result = leftValue + rightValue;
            } else {
                result = leftValue - rightValue;
            }

            return createNumericLiteral(left, result);
        }

        if (operation instanceof MultiplyOperation) {
            int result = leftValue * rightValue;

            if (left instanceof ScalarLiteral) {
                return createNumericLiteral(right, result);
            }

            if (right instanceof ScalarLiteral) {
                return createNumericLiteral(left, result);
            }

            throw new IllegalArgumentException(
                    "Vermenigvuldigen vereist minstens 1 scalar."
            );
        }

        throw new IllegalArgumentException(
                "Onbekende bewerking: "
                        + operation.getClass().getSimpleName()
        );
    }

    private boolean isNumeric(Literal literal) {
        return literal instanceof PixelLiteral
                || literal instanceof PercentageLiteral
                || literal instanceof ScalarLiteral;
    }

    private int getNumericValue(Literal literal) {
        if (literal instanceof PixelLiteral) {
            return ((PixelLiteral) literal).value;
        }

        if (literal instanceof PercentageLiteral) {
            return ((PercentageLiteral) literal).value;
        }

        if (literal instanceof ScalarLiteral) {
            return ((ScalarLiteral) literal).value;
        }

        throw new IllegalArgumentException("Geen numerieke waarde.");
    }

    private Literal createNumericLiteral(Literal example, int value) {

        if (example instanceof PixelLiteral) {
            return new PixelLiteral(value);
        }

        if (example instanceof PercentageLiteral) {
            return new PercentageLiteral(value);
        }

        if (example instanceof ScalarLiteral) {
            return new ScalarLiteral(value);
        }

        throw new IllegalArgumentException("Geen numeriek resultaattype.");
    }
}
