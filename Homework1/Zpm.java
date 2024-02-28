import java.io.*; // Import classes for file I/O
import java.nio.file.*; // Import classes for working with file paths
import java.util.*; // Import classes for maps and collections
import java.util.stream.Collectors;
import java.util.List;
import java.util.logging.Logger; // Import classes for logging error handling

public class Zpm {
    // Map to store variables and their values

    private static final String VAR_PATTERN = "[A-Z]";
    private static final String INTEGER_PATTERN = "-?\\d+";
    private static final Map<String, Object> variables = new HashMap<>();

    public static void main(String[] args) throws RuntimeException {
        Logger logger = Logger.getLogger("Zpm");

        if (args.length != 1 || !args[0].endsWith(".zpm")) {
            logger.severe("Error: Please provide a .zpm file as an argument.");
            return;
        }

        String fileName = args[0];
        try {
            // Read all lines from the script file
            List<String> lines = Files.readAllLines(Paths.get(fileName));
            List<String> trimmedLines = trimLines(lines);
            processLines(trimmedLines); // Process each trimmed line

        } catch (IOException e) {
            logger.severe("Error reading file: " + e.getMessage()); // Catch file reading errors
        } catch (RuntimeException e) {
            logger.severe("Runtime Error: " + e.getMessage()); // Catch runtime errors
        } catch (Exception e) {
            throw new IllegalStateException();
        }
    }
    private static List<String> trimLines(List<String> lines) {
        return lines.stream().map(String::trim).collect(Collectors.toList());    }
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

    // Inside the Zpm class, replace the existing processAssignment method with this:

    private static void processAssignment(String line, int lineNum) throws Exception {
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
    private static void throwInvalidOperationException(int lineNum) throws IllegalArgumentException {
        throw new IllegalArgumentException("RUNTIME ERROR: Invalid operation or type mismatch at line " + lineNum);
    }

    private static void modifyValue(String varName, String value, String operation, int lineNum) throws IllegalArgumentException {
        if (!variables.containsKey(varName)) {
            throw new IllegalArgumentException("RUNTIME ERROR: Variable " + varName + " used before initialization at line " + lineNum);
        }
        Object varValue = variables.get(varName);
        if (operation.equals("+=")) {
            if (varValue instanceof Integer && value.matches(INTEGER_PATTERN)) {
                variables.put(varName, (Integer) varValue + Integer.parseInt(value));
            } else if (varValue instanceof String && value.startsWith("\"") && value.endsWith("\"")) {
                variables.put(varName, varValue.toString() + value.substring(1, value.length() - 1));
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

    private static int processForLoop(List<String> lines, int lineNum) throws Exception {
        String[] parts = lines.get(lineNum - 1).split(" ");
        int loopCount = Integer.parseInt(parts[1]);
        int endLineNum = findEndForLineNum(lines, lineNum);
        List<String> loopLines = lines.subList(lineNum, endLineNum);

        for (int i = 0; i < loopCount; i++) {
            for (String loopLine : loopLines) {
                processAssignment(loopLine, lineNum);
            }
        }

        return endLineNum;
    }

    private static void processPrintStatement(String line, int lineNum) throws IllegalArgumentException{
        String varName = line.substring(6, line.length() -1).trim();

        if (!variables.containsKey(varName)) {
            throw new IllegalArgumentException("RUNTIME ERROR: line " + lineNum);
        }
        System.out.println(varName + "=" + variables.get(varName));
    }
}

// Created with care by James Beaupry, with assistance from the following tools:
// ChatGPT 4, Google Gemini, IntelliJ AI Assistant