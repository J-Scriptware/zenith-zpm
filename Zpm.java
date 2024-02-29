import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * The Zpm class represents a program that reads and processes Zpm code.
 * It contains methods for validating and reading an input file, processing individual lines of code,
 * handling exceptions, performing operations, and executing Zpm statements.
 */
public class Zpm {

    protected static final String STRING_PATTERN = "\".*\"";
    protected static final String INTEGER_PATTERN = "^-?\\d+$";
    protected static final String PRINT = "PRINT ";
    protected static final int PRINT_PREFIX_LENGTH = PRINT.length();

    /**
     * This variable stores a collection of operator functions for performing operations on objects.
     * The key is a String representing the operator symbol, and the value is a BiFunction that takes two
     * objects as input and returns an object as the result of the operation.
     */
    protected static final HashMap<String, BiFunction<Object, Object, Object>> operators;

    static {
        operators = new HashMap<>();
        operators.put("=", Object::equals);
        operators.put("+=", Zpm::additionOperator);
        operators.put("-=", Zpm::subtractionOperator);
        operators.put("*=", Zpm::multiplicationOperator);
    }

    /**
     * A Map that stores variables as key-value pairs.
     * The keys are Strings representing the variable names,
     * and the values are Objects representing the variable values.
     */
    protected static final Map<String, Object> variables = new HashMap<>();

    /**
     * This variable stores a list of strings representing lines of ZPM code.
     */
    protected static List<String> lines = new ArrayList<>();

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
            throw new ZpmRuntimeException("An unexpected error occurred", 0);
        }
    }

    /**
     * Validates and reads the contents of the input file specified as a command line argument.
     * Throws an exception if the input file is invalid or does not exist.
     *
     * @param args The command line arguments. The first argument should be the path to the input file.
     * @return A list of strings representing the lines of the input file.
     * @throws ZpmRuntimeException      If an unexpected error occurs during the execution of the Zpm program.
     * @throws IllegalArgumentException If the input file is invalid or does not exist.
     * @throws IOException              If an IO error occurs while reading the input file.
     */
    private static List<String> validateAndReadFile(String[] args) throws ZpmRuntimeException, IllegalArgumentException, IOException {
        if (args.length != 1 || !args[0].endsWith(".zpm")) {
            throw new IllegalArgumentException("Error: Please provide a .zpm file as an argument.");
        }
        Path filePath;
        try {
            filePath = Paths.get(args[0]);
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException("Invalid path provided as argument", e);
        }
        if (!Files.exists(filePath)) {
            throw new IOException("Error: File does not exist.");
        }
        return Files.readAllLines(filePath);
    }

    /**
     * Processes a single line of code in the Zpm program.
     *
     * @param line    The line of code to be processed.
     * @param lineNum The line number of the code in the input file.
     * @throws IllegalArgumentException If the line provided is null or does not start with a valid keyword.
     * @throws ZpmRuntimeException      If an error occurs during the processing of the line.
     */
    public static void processLine(String line, int lineNum) {
        if (line.startsWith(PRINT)) {
            processPrintStatement(line, lineNum);
        } else if (line.startsWith("FOR ")) {
            processForLoop(line, lineNum);
        } else {
            processAssignment(line, lineNum);
        }
    }

    /**
     * Prints an error message based on the given exception.
     * If the exception is an instance of IOException, it prints "I/O Error: " followed by the exception message.
     * If the exception is an instance of ZpmRuntimeException, it prints the exception message.
     * For any other exception, it prints "Unexpected Error: " followed by the exception message.
     *
     * @param e The exception to be printed.
     */
    public static void printError(Exception e) {
        if (e instanceof IOException) {
            System.out.println("I/O Error: " + e.getMessage());
        } else if (e instanceof ZpmRuntimeException) {
            System.out.println(e.getMessage());
        } else {
            System.out.println("Unexpected Error: " + e.getMessage());
        }
    }

    /**
     * Processes an assignment statement in the Zpm program.
     *
     * @param line    The assignment statement to be processed.
     * @param lineNum The line number of the assignment statement in the input file.
     * @throws ZpmRuntimeException If an unrecognized operation or invalid format is found in the assignment statement.
     */
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

    /**
     * Assigns or modifies the value of a variable based on the given parameters.
     *
     * @param varName   The name of the variable.
     * @param valueStr  The value of the variable as a String.
     * @param operation The operation to perform on the variable. Valid operations are "+=", "-=", "*=", and "=".
     * @param lineNum   The line number in the input file where the assignment is made.
     * @throws ZpmRuntimeException If the valueStr is not a valid integer or string, or if the variable name is unrecognized.
     */
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

    /**
     * Performs an operation on a variable based on the given parameters.
     *
     * @param varName   The name of the variable.
     * @param newValue  The new value to be applied to the variable.
     * @param operation The operation to perform on the variable. Valid operations are "+=", "-=", "*=", and "=".
     * @param lineNum   The line number in the input file where the operation is performed.
     * @throws ZpmRuntimeException If an unsupported operation or type mismatch occurs.
     */
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

    /**
     * Processes a FOR loop statement in the Zpm program.
     *
     * @param line    The line containing the FOR loop statement.
     * @param lineNum The line number of the FOR loop statement in the input file.
     * @throws RuntimeException If the FOR loop syntax is invalid or if any errors occur during the execution of the loop.
     */
    private static void processForLoop(String line, int lineNum) {
        String[] parts = line.split(" ", 3); // Split on the first two spaces

        if (parts.length != 3) {
            throw new ZpmRuntimeException("Expected format: FOR n <statements> ENDFOR. Invalid FOR loop at line ", lineNum);
        }

        // Extract loop count
        int loopCount;
        try {
            loopCount = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            throw new ZpmRuntimeException("Invalid loop count in FOR loop at line ", lineNum);
        }

        // Detect invalid loop count
        if (loopCount <= 0) {
            throw new ZpmRuntimeException("FOR loop count should be > 0 at line ", lineNum);
        }

        // Split loop statements by ";"
        String[] assignments = parts[2].replace("; ENDFOR", "").split(";");

        // Execute loop
        for (int i = 0; i < loopCount; i++) {
            for (String assignment : assignments) {
                // Process each assignment statement inside the loop
                processAssignment(assignment.trim(), lineNum);
            }
        }
    }

    /**
     * Processes a PRINT statement in the Zpm program.
     *
     * @param line    The line of code to be processed.
     * @param lineNum The line number of the code in the input file.
     * @throws IllegalArgumentException  If the line provided is null or does not start with the PRINT keyword.
     * @throws ZpmRuntimeException       If an error occurs during the processing of the PRINT statement.
     * @throws IndexOutOfBoundsException If the format of the PRINT statement is invalid.
     */
    private static void processPrintStatement(String line, int lineNum) {
        if (line == null || !line.startsWith(PRINT)) {
            throw new IllegalArgumentException("Invalid line provided for processPrintStatement");
        }
        String varName;
        try {
            varName = line.substring(PRINT_PREFIX_LENGTH).replace(";", "").trim();
        } catch (IndexOutOfBoundsException e) {
            throw new ZpmRuntimeException("Invalid format of PRINT statement at line: " + lineNum, lineNum);
        }
        Object value = Zpm.variables.get(varName);
        if (value == null) {
            throw new ZpmRuntimeException("Unknown variable '" + varName + "'", lineNum);
        }

        System.out.println(varName + "=" + value);
    }

    /**
     * Performs addition operation on two objects.
     *
     * @param a The first object.
     * @param b The second object.
     * @return The result of the addition operation.
     * @throws IllegalArgumentException If the objects are not of compatible types for addition.
     */
    protected static Object additionOperator(Object a, Object b) {
        return switch (a) {
            case Integer i when b instanceof Integer -> i + ((Integer) b);
            case String s when b instanceof String -> s + b;
            case String s when b instanceof Integer -> s + b;
            case Integer i when b instanceof String -> i + (String) b;
            case null, default -> throw new IllegalArgumentException("Mismatched types for '+=' operation");
        };
    }

    /**
     * Performs subtraction operation on two objects.
     *
     * @param a The minuend.
     * @param b The subtrahend.
     * @return The result of the subtraction operation.
     * @throws IllegalArgumentException If the objects are not of compatible types for subtraction.
     */
    protected static Object subtractionOperator(Object a, Object b) {
        if (a instanceof Integer && b instanceof Integer) {
            return ((Integer) a) - ((Integer) b);
        } else {
            throw new IllegalArgumentException("'-=' operation only supported for integers");
        }
    }

    /**
     * Performs multiplication operation on two objects.
     *
     * @param a The first object.
     * @param b The second object.
     * @return The result of the multiplication operation.
     * @throws IllegalArgumentException If the objects are not of compatible types for multiplication.
     */
    protected static Object multiplicationOperator(Object a, Object b) {
        if (a instanceof Integer && b instanceof Integer) {
            return ((Integer) a) * ((Integer) b);
        } else {
            throw new IllegalArgumentException("'*=' operation only supported for integers");
        }
    }

    public static class ZpmRuntimeException extends RuntimeException {
        ZpmRuntimeException(String message, int lineNum) {
            super(message + ": line " + lineNum);
        }
    }
}

