import java.util.HashMap;
import java.util.Map;

public class Zpm {
    // HashMap to store variables and their values during program execution
    private Map<String, Object> variables = new HashMap<>();

    public static void main(String[] args) {
        Zpm zpm = new Zpm();
        zpm.runProgram(new String[]{
                "A = 12 ;",
                "PRINT A ;",
                "A += 34 ;",
                "PRINT A ;",
                "A = \"hello\" ;",
                "A += \" world\" ;",
                "PRINT A ;",
                "FOR 2 PRINT A ; A += \"!\" ; ENDFOR",
                "PRINT A ;"
        });
    }

    // Loads and executes a program line by line
    public void runProgram(String[] program) {
        for (String statement : program) {
            executeStatement(statement.trim()); // Remove leading/trailing whitespace
        }
    }

    // Determines the type of statement and executes the appropriate logic
    private void executeStatement(String statement) {
        if (statement.startsWith("PRINT")) {
            executePrint(statement);
        } else if (statement.contains("=") || statement.contains("+=") || statement.contains("*=") || statement.contains("-=")) {
            executeAssignment(statement);
        } else if (statement.startsWith("FOR")) {
            executeForLoop(statement);
        }
    }

    // Handles the 'PRINT' statement
    private void executePrint(String statement) {
        String varName = statement.substring(6, statement.length() - 1).trim();

        // Check if the variable exists before printing
        if (!variables.containsKey(varName)) {
            throw new RuntimeException("Runtime Error: Variable " + varName + " not initialized.");
        }

        System.out.println(variables.get(varName));
    }

    // Handles assignment statements (both simple and compound assignments)
    private void executeAssignment(String statement) {
        if (statement.contains("FOR")) {
            executeForLoop(statement.trim());
        } else {
            String[] parts = statement.split(" ");
            String varName = parts[0];
            String operator = parts[1];
            String value = statement.substring(varName.length() + operator.length() + 2, statement.length() - 1).trim();

            // Handle simple assignment (=)
            if (operator.equals("=")) {
                if (value.matches("-?\\d+")) { // Integer assignment
                    variables.put(varName, Integer.parseInt(value));
                } else if (value.startsWith("\"")) { // String assignment
                    variables.put(varName, value.substring(1, value.length() - 1));
                } else if (variables.containsKey(value)) { // Variable assignment
                    variables.put(varName, variables.get(value));
                } else {
                    throw new RuntimeException("Runtime Error: Invalid assignment value.");
                }
            } else {
                // Handle compound assignments (+=, -=, etc.)
                if (!variables.containsKey(varName)) {
                    throw new RuntimeException("Runtime Error: Variable " + varName + " not initialized.");
                }
                Object varValue = variables.get(varName);
                executeCompoundAssignment(varName, operator, value, varValue);
            }
        }
    }

    // Handles compound assignment operations
    private void executeCompoundAssignment(String varName, String operator, String value, Object varValue) {
        // Assuming that the values are of compatible types
        switch (operator) {
            case "+=":
                if (varValue instanceof String) {
                    variables.put(varName, varValue + value.substring(1, value.length() - 1));
                } else if (varValue instanceof Integer) {
                    variables.put(varName, (Integer) varValue + Integer.parseInt(value));
                } else {
                    throw new RuntimeException("Runtime Error: Incompatible types for '+=' operation.");
                }
                break;
            // Add cases for other compound assignment operators (*=, -=) ...
            default:
                throw new RuntimeException("Runtime Error: Unknown operator " + operator);
        }
    }

    // Handles FOR loop execution (needs improvement for a fully featured implementation)
    private void executeForLoop(String statement) {
        // Correct parsing and execution of FOR loop statement needed
        String[] parts = statement.split(" ");
        int loopCount = Integer.parseInt(parts[1]);
        String loopBody = statement.substring(statement.indexOf(" ") + parts[1].length() + 1, statement.lastIndexOf("ENDFOR")).trim();
        String[] loopStatements = loopBody.split(" ; ");

        for (int i = 0; i < loopCount; i++) {
            for (String loopStatement : loopStatements) {
                executeStatement(loopStatement + ";");
            }
        }
    }
}

// Created with care by James Beaupry, with assistance from the following tools:
// Chat GPT 4, Google Gemini, IntelliJ AI Assistant