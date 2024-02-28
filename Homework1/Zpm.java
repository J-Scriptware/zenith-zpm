import java.io.*; // Import classes for file I/O
import java.nio.file.*; // Import classes for working with file paths
import java.util.*; // Import classes for maps and collections
import java.util.stream.Collectors;
import java.util.List;

/**
 * The Zpm class is a simple interpreter for the Zpm programming language.
 * It supports variable assignment, arithmetic operations, loops, and printing variables.
 * The class contains a main method for executing Zpm programs.
 */
public class Zpm {
    // Map to store variables and their values

    private static final String VAR_PATTERN = "[A-Z]";
    private static final String INTEGER_PATTERN = "-?\\d+";
    private static final Map<String, Object> variables = new HashMap<>();

    /**
     * The main method is the entry point of the program. It takes an array of string arguments,
     * checks if the argument is a valid file with a ".zpm" extension, reads the lines from the file,
     * trims the lines, and processes each line accordingly.
     *
     * @param args The command line arguments passed to the program. The first argument should be the file path
     *             of a ".zpm" file.
     * @throws ZpmRuntimeException If there is an error reading the file or processing the lines.
     */
    public static void main(String[] args) throws RuntimeException {
        if (args.length != 1 || !args[0].endsWith(".zpm")) {
            System.out.println("Error: Please provide a .zpm file as an argument.");
            return;
        }

        Path filePath = Paths.get(args[0]);
        if (!Files.exists(filePath)) {
            System.out.println("Error: File does not exist.");
            return;
        }
        try {
            List<String> lines = Files.readAllLines(filePath);
            lines = trimLines(lines);
            processLines(lines);
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    /**
     * Trims the leading and trailing whitespace from each line in the given list.
     *
     * @param lines The list of strings to trim.
     * @return A new list of strings with the leading and trailing whitespace trimmed.
     */
    private static List<String> trimLines(List<String> lines) {
        return lines.stream().map(String::trim).collect(Collectors.toList());    }

    /**
     * Processes a list of lines, executing the instructions contained within.
     *
     * @param lines the list of lines to process
     */
    private static void processLines(List<String> lines) {
        int lineNum = 0;

        for (String line : lines) {
            lineNum++;
            if (line.trim().isEmpty()) continue;

            if (line.startsWith("PRINT ")) {
                processPrintStatement(line, lineNum);
            } else if (line.startsWith("FOR ")) {
                lineNum = processForLoop(lines, lineNum);
            } else {
                processAssignment(line, lineNum);
            }
        }
    }

    /**
     * Processes an assignment statement provided in a string format.
     *
     * @param line    the assignment statement to be processed
     * @param lineNum the line number of the assignment statement in the input file
     * @throws IllegalArgumentException if the variable name is invalid or if the operation is unsupported
     */
    private static void processAssignment(String line, int lineNum) throws IllegalArgumentException {
        String[] parts = line.split(" ", 3); // Split by the first occurrence of space to accommodate compound assignments
        String varName = parts[0].trim();
        if (!varName.matches(VAR_PATTERN)) {
            throw new IllegalArgumentException("RUNTIME ERROR: Invalid variable name at line " + lineNum);
        }

        // Determine the type of assignment
        String operation = parts[1].trim();
        String value = parts[2].trim().replace(";", "");

        switch (operation) {
            case "=":
                assignValue(varName, value, lineNum);
                break;
            case "+=":
            case "*=":
            case "-=":
                modifyValue(varName, value, operation, lineNum);
                break;
            default:
                throw new IllegalArgumentException("RUNTIME ERROR: Unsupported operation at line " + lineNum);
        }
    }

    /**
     * Assigns a value to a variable.
     *
     * @param varName  The name of the variable to assign a value to.
     * @param value    The value to assign to the variable.
     * @param lineNum  The line number at which this assignment is performed.
     * @throws IllegalArgumentException if the variable name is invalid or if the assignment is unsupported.
     */
    private static void assignValue(String varName, String value, int lineNum) throws IllegalArgumentException {
        if (value.matches(INTEGER_PATTERN)) { // Integer assignment
            variables.put(varName, Integer.parseInt(value));
        } else if (value.startsWith("\"") && value.endsWith("\"")) { // String assignment
            variables.put(varName, value.substring(1, value.length() - 1));
        } else if (variables.containsKey(value)) { // Variable assignment
            variables.put(varName, variables.get(value));
        } else {
            throw new IllegalArgumentException("RUNTIME ERROR: Undefined variable " + value + " at line " + lineNum);
        }
    }

    /**
     * Throws an {@link IllegalArgumentException} with a specific error message indicating an invalid operation or type mismatch.
     *
     * @param lineNum the line number where the invalid operation or type mismatch occurred.
     * @throws IllegalArgumentException always throws this exception with the specific error message.
     */
    private static void throwInvalidOperationException(int lineNum) throws IllegalArgumentException {
        throw new IllegalArgumentException("RUNTIME ERROR: Invalid operation or type mismatch at line " + lineNum);
    }

    /**
     * Modifies the value of a variable based on the given operation.
     *
     * @param varName   the name of the variable to modify
     * @param value     the value to use for the operation
     * @param operation the operation to perform on the variable
     * @param lineNum   the line number where the method is called
     * @throws IllegalArgumentException if the variable is not initialized or there is a type mismatch
     */
    private static void modifyValue(String varName, String value, String operation, int lineNum) throws IllegalArgumentException {
        if (!variables.containsKey(varName)) {
            throw new IllegalArgumentException("RUNTIME ERROR: Variable " + varName + " used before initialization at line " + lineNum);
        }
        Object varValue = variables.get(varName);
        if (operation.equals("+=")) {
            if (varValue instanceof Integer && value.matches(INTEGER_PATTERN)) {
                variables.put(varName, (Integer) varValue + Integer.parseInt(value));
            } else if (varValue instanceof String && value.startsWith("\"") && value.endsWith("\"")) {
                variables.put(varName, varValue + value.substring(1, value.length() - 1));
            } else if(variables.containsKey(value)) {
                variables.put(varName, varValue.toString() + variables.get(value).toString());
            } else {
                throw new IllegalArgumentException("RUNTIME ERROR: Type mismatch for += at line " + lineNum);
            }
        } else {
            if (varValue instanceof Integer && value.matches(INTEGER_PATTERN)) {
                int intValue = Integer.parseInt(value);
                switch (operation) {
                    case "*=":
                        variables.put(varName, (Integer) varValue * intValue);
                        break;
                    case "-=":
                        variables.put(varName, (Integer) varValue - intValue);
                        break;
                    default:
                        throwInvalidOperationException(lineNum);
                }
            } else {
                throwInvalidOperationException(lineNum);
            }
        }
    }
    /**
     * Processes a for loop block of code.
     *
     * @param lines           The list of lines containing the for loop block.
     * @param currentLineNum  The current line number where the for loop block starts.
     * @return The line number after the ENDFOR statement.
     * @throws ZpmRuntimeException If there is an error while processing the for loop block.
     */
    private static int processForLoop(List<String> lines, int currentLineNum) throws ZpmRuntimeException {
        String[] parts = lines.get(currentLineNum - 1).split(" ");
        int loopCount = Integer.parseInt(parts[1]);
        int loopEnd = findEndForLineNum(lines, currentLineNum);

        for (int j = 0; j < loopCount; j++) {
            for (int i = currentLineNum; i < loopEnd; i++) {
                String loopLine = lines.get(i).trim();
                processAssignment(loopLine, i + 1); // Adjust line number for accurate error reporting
            }
        }

        return loopEnd + 1; // Return the line number after ENDFOR
    }

    private static int findEndForLineNum(List<String> lines, int startLineNum) {
        for (int i = startLineNum; i < lines.size(); i++) {
            if (lines.get(i).trim().equals("ENDFOR")) {
                return i; // Return the actual line number of ENDFOR for further processing
            }
        }
        return startLineNum; // Should never happen if input is correct, but safe fallback
    }


    /**
     * Processes a PRINT statement provided in a string format.
     *
     * @param line    the PRINT statement to be processed
     * @param lineNum the line number of the PRINT statement in the input file
     * @throws IllegalArgumentException if the variable name is invalid or if the variable is not found
     */
    private static void processPrintStatement(String line, int lineNum) throws IllegalArgumentException{
        String varName = line.substring(6, line.length() -1).trim();

        if (!variables.containsKey(varName)) {
            throw new IllegalArgumentException("RUNTIME ERROR: line " + lineNum);
        }
        System.out.println(varName + "=" + variables.get(varName));
    }
    protected static class ZpmRuntimeException extends RuntimeException {
        private final int lineNum;

        ZpmRuntimeException(String message, int lineNum) {
            super(message);
            this.lineNum = lineNum;
        }

        @SuppressWarnings("unused")
        int getLineNum() {
            return lineNum;
        }
    }
}

// Created with care by James Beaupry, with assistance from the following tools:
// IntelliJ AI Assistant, ChatGPT 4