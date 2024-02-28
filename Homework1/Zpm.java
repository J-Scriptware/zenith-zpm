import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class Zpm {
    public static final String STRING_PATTERN = "\".*\"";
    public static final String INTEGER_PATTERN = "^-?\\d+$";
    public static final String PRINT = "PRINT ";
    protected static final HashMap<String, BiFunction<Object, Object, Object>> operators;
    protected static final Map<String, Object> variables = new HashMap<>();
    // Add line here:
    protected static List<String> lines = new ArrayList<>();

    static {
        operators = new HashMap<>();
        operators.put("=", (a, b) -> b);
        operators.put("+=", (a, b) -> {
            if (a instanceof Integer && b instanceof Integer) {
                return ((Integer) a) + ((Integer) b);
            } else if (a instanceof String && b instanceof String) {
                return a + (String) b;
            } else {
                throw new IllegalArgumentException("Mismatched types for '+=' operation");
            }
        });
        operators.put("-=", (a, b) -> {
            if (a instanceof Integer && b instanceof Integer) {
                return ((Integer) a) - ((Integer) b);
            } else {
                throw new IllegalArgumentException("'-=' operation only supported for integers");
            }
        });
        operators.put("*=", (a, b) -> {
            if (a instanceof Integer && b instanceof Integer) {
                return ((Integer) a) * ((Integer) b);
            } else {
                throw new IllegalArgumentException("'*=' operation only supported for integers");
            }
        });
    }

    private static void processAssignment(String line, int lineNum) throws ZpmRuntimeException {
        String[] operations = new String[]{"+=", "-=", "*=", "="};
        String[] parts = null;
        String operation = null;

        for (String op : operations) {
            if (line.contains(op)) {
                parts = line.split("\\s*\\Q" + op + "\\E\\s*", 2);
                operation = op;
                break;
            }
        }

        if (operation == null) {
            throw new ZpmRuntimeException("Unrecognized operation in assignment statement at line ", lineNum);
        }

        if (parts.length != 2) {
            throw new ZpmRuntimeException("Expected format: variable operator expression. Invalid assignment statement at line ", lineNum);
        }

        String varName = parts[0].trim();
        String valueStr = parts[1].trim().replace(";", "").trim();

        assignOrModifyValue(varName, valueStr, operation, lineNum);
    }

    private static void assignOrModifyValue(String varName, String valueStr, String operation, int lineNum) {
        Object newValue;

        if (valueStr.matches(INTEGER_PATTERN)) {
            newValue = Integer.parseInt(valueStr);
        } else if (valueStr.matches(STRING_PATTERN)) {
            newValue = valueStr.substring(1, valueStr.length() - 1);
        } else {
            // If the valueStr is not an Integer or a String pattern,
            // check if it's a variable already present in the map
            // If it's none of the above, it's an unrecognized variable name
            if (variables.containsKey(valueStr)) {
                newValue = variables.get(valueStr);
            } else {
                throw new ZpmRuntimeException("Unrecognized variable '" + valueStr + "'", lineNum);
            }
        }

        performOperation(varName, newValue, operation, lineNum);
    }

    private static void performOperation(String varName, Object newValue, String operation, int lineNum) {
        var operator = operators.get(operation);
        if (operator == null) {
            throw new ZpmRuntimeException("Unsupported operation or type mismatch at line number: ", lineNum);
        }

        if (variables.containsKey(varName)) {
            Object existingValue = variables.get(varName);
            if (existingValue != null && (!(existingValue instanceof Integer)) && (!(existingValue instanceof String)) && !operation.equals("=")) {
                throw new ZpmRuntimeException("Existing value for " + varName + " is not an integer or a string, cannot perform operation", lineNum);
            }
            if (newValue != null) {
                variables.put(varName, operator.apply(existingValue, newValue));
            }
        } else {
            if (newValue != null) {
                variables.put(varName, newValue);
            }
        }
    }

    private static void processForLoop(String line, int currentLineNum) {
        String[] parts = line.split(" ", 2);

        if (parts.length < 2 || !parts[1].matches("\\d+ .*")) {
            throw new ZpmRuntimeException("Invalid syntax for FOR loop at line ", currentLineNum);
        }

        int loopCount;
        try {
            loopCount = Integer.parseInt(parts[1].split(" ", 2)[0]);
        } catch (NumberFormatException e) {
            throw new ZpmRuntimeException("Invalid loop count in FOR statement at line ", currentLineNum);
        }

        if (loopCount <= 0) {
            throw new ZpmRuntimeException("Loop count should be a positive number at line ", currentLineNum);
        }

        // We are replacing all ENDFOR with ""; as we don't have nested loops and each FOR matches with the next ENDFOR
        String loopBody = parts[1].replace("ENDFOR", "").trim();
        String[] stmtsInsideLoop = loopBody.split(";");

        for (int i = 0; i < loopCount; i++) {
            for (String stmt : stmtsInsideLoop) {
                stmt = stmt.trim(); // Trim the statement to remove potential leading or trailing spaces
                if (!stmt.isEmpty()) {
                    processStatement(stmt, currentLineNum);
                }
            }
        }
    }

    private static void processStatement(String statement, int lineNum) {
        if (statement.startsWith(PRINT)) {
            processPrintStatement(statement, lineNum);
        } else if (statement.startsWith("FOR ")) {
            processForLoop(statement, lineNum);
        } else {
            processAssignment(statement, lineNum);
        }
    }

    private static void processPrintStatement(String line, int lineNum) {
        if (line == null || !line.startsWith(PRINT)) {
            throw new IllegalArgumentException("Invalid line provided for processPrintStatement");
        }

        String varName;
        try {
            varName = line.substring(6).replace(";", "").trim();
            System.out.println(varName);
        } catch (IndexOutOfBoundsException e) {
            throw new ZpmRuntimeException("Invalid format of PRINT statement at line: " + lineNum, lineNum);
        }

        Object value = Zpm.variables.get(varName);
        if (value == null) {
            throw new ZpmRuntimeException("Unknown variable '" + varName + "'", lineNum);
        }
        synchronized (Zpm.variables) {
            System.out.println(varName + "=" + value);
        }
    }

    public static void processLine(String line, int lineNum) {
        if (line.startsWith(PRINT)) {
            processPrintStatement(line, lineNum);
        } else if (line.startsWith("FOR ")) {
            processForLoop(line, lineNum);
        } else {
            processAssignment(line, lineNum);
        }
    }

    public static void printError(Exception e) {
        if (e instanceof IOException) {
            System.out.println("I/O Error: " + e.getMessage());
        } else if (e instanceof ZpmRuntimeException) {
            System.out.println(e.getMessage());
        } else {
            System.out.println("Unexpected Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws ZpmRuntimeException {
        try {
            lines = validateAndReadFile(args);  // Assign the read lines to the static attribute
            lines.removeIf(String::isEmpty);
            lines = lines.stream().map(String::trim).collect(Collectors.toList());
            int lineNum = 1;
            for (String line : lines) {
                processLine(line, lineNum);
                lineNum++;
            }
        } catch (Exception e) {
            printError(e);
        }
    }

    private static List<String> validateAndReadFile(String[] args) throws ZpmRuntimeException, IOException {
        if (args.length != 1 || !args[0].endsWith(".zpm")) {
            throw new IOException("Error: Please provide a .zpm file as an argument.");
        }
        Path filePath = Paths.get(args[0]);
        if (!Files.exists(filePath)) {
            throw new IOException("Error: File does not exist.");
        }
        return Files.readAllLines(filePath);
    }

    public static class ZpmRuntimeException extends RuntimeException {
        ZpmRuntimeException(String message, int lineNum) {
            super(message + ": line " + lineNum);
        }
    }
}

