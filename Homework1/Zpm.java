import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.List;

public class Zpm {
    private static final Map<String, Object> variables = new HashMap<>();
    /**
     * Binary operators for ZPM operations
     */
    protected static final HashMap<String, BinaryOperator> operators = new HashMap<>();
    // Initialize operators
    static {
        operators.put("+=", (o1, o2) -> (Integer) o1 + (Integer) o2);
        operators.put("-=", (o1, o2) -> (Integer) o1 - (Integer) o2);
        operators.put("*=", (o1, o2) -> (Integer) o1 * (Integer) o2);
    }

    public static class ZpmRuntimeException extends RuntimeException {
        ZpmRuntimeException(String message, int lineNum) {
            super(message + ": line " + lineNum);
        }
    }

    public static void main(String[] args) {
        try {
            List<String> lines = validateAndReadFile(args);
            lines = trimLines(lines);
            processLines(lines);
        } catch (IOException e) {
            System.out.println("I/O Error: " + e.getMessage());
        } catch (ZpmRuntimeException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<String> validateAndReadFile(String[] args) throws ZpmRuntimeException, IOException {
        if (args.length != 1 || !args[0].endsWith(".zpm")) {
            throw new IllegalArgumentException("Error: Please provide a .zpm file as an argument.");
        }
        Path filePath = Paths.get(args[0]);
        if (!Files.exists(filePath)) {
            throw new IOException("Error: File does not exist.");
        }
        return Files.readAllLines(filePath);
    }

    private static List<String> trimLines(List<String> lines) {
        return lines.stream().map(String::trim).collect(Collectors.toList());
    }

// Must refactor assignOrModifyValue varName, String valueStr, String operation, int lineNum with updated logic

    private static void processLines(List<String> lines) throws ZpmRuntimeException {
        int lineNum = 1;
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            if (line.matches("^PRINT\\s.*")) {
                processPrintStatement(line, lineNum);
            } else if (line.matches("^FOR\\s.*")) {
                lineNum = processForLoop(lines, lineNum);
            } else {
                processAssignment(line, lineNum);
            }
            lineNum++;
        }
    }

    private static void processAssignment(String line, int lineNum) throws ZpmRuntimeException {
        String[] parts;
        String operation;
        if(line.contains("+=") || line.contains("-=") || line.contains("*=")) {
            parts = line.split("(\\+=|-=|\\*=)", 2);
            operation = line.contains("+=") ? "+=" : line.contains("-=") ? "-=" : "*=";
        } else {
            parts = line.split("=", 2);
            operation = "=";
        }
        if (parts.length != 2) {
            throw new ZpmRuntimeException("Invalid assignment statement at", lineNum);
        }
        String varName = parts[0].trim();
        String valueStr = parts[1].trim().replace(";", "");

        assignOrModifyValue(varName, valueStr, operation, lineNum);
    }

    private static void assignOrModifyValue(String varName, String value, String operation, int lineNum) throws ZpmRuntimeException {
        Object newValue = getNewValue(value);

        if (operation == null) {
            variables.put(varName, newValue);
            return;
        }

        performOperation(varName, newValue, operation, lineNum);
    }

    private static Object getNewValue(String value) {
        final String STRING_PATTERN = "\".*\"";
        final String INTEGER_PATTERN = "^-?\\d+$";
        Object newValue = new Object();

        if (value.matches(STRING_PATTERN)) {
            newValue = value.substring(1, value.length() - 1);
        } else if (value.matches(INTEGER_PATTERN)) {
            newValue = Integer.parseInt(value);
        } else if (variables.containsKey(value)) {
            newValue = variables.get(value);
        }
        return newValue;
    }



    private static void performOperation(String varName, Object newValue, String operation, int lineNum) {
        Object varValue = variables.get(varName);
        if (varValue instanceof Integer) {
            @SuppressWarnings("unchecked")
            BinaryOperator<Integer> operator = operators.get(operation);
            if (operator == null) {
                throw new ZpmRuntimeException("Unsupported operation or type mismatch at", lineNum);
            }
            variables.put(varName, operator.apply((Integer) varValue, (Integer) newValue));
        } else {
            throw new ZpmRuntimeException("Value is not an integer, cannot perform operation at", lineNum);
        }
    }
    private static int processForLoop(List<String> lines, int currentLineNum) throws ZpmRuntimeException {
        int loopCount = getLoopCount(lines, currentLineNum);
        int loopEnd = findEndForLineNum(lines, currentLineNum);
        for (int j = 0; j < loopCount; j++) {
            for (int i = currentLineNum; i < loopEnd; i++) {
                String loopLine = lines.get(i).trim();
                if (loopLine.startsWith("PRINT ")) {
                    processPrintStatement(loopLine, i + 1);
                } else {
                    processAssignment(loopLine, i + 1);
                }
            }
        }
        return loopEnd;
    }

    private static int getLoopCount(List<String> lines, int currentLineNum) {
        String line = lines.get(currentLineNum - 1);
        String[] parts = line.split(" ");
        if (parts.length < 2 || !parts[0].equals("FOR")) {
            throw new ZpmRuntimeException("Invalid syntax for FOR loop at", currentLineNum);
        }
        int loopCount;
        try {
            loopCount = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            throw new ZpmRuntimeException("Invalid loop count in FOR statement at", currentLineNum);
        }
        if (loopCount <= 0) {
            throw new ZpmRuntimeException("Loop count should be a positive integer at", currentLineNum);
        }
        return loopCount;
    }

    private static int findEndForLineNum(List<String> lines, int startLineNum) {
        for (int i = startLineNum; i < lines.size(); i++) {
            if (lines.get(i).trim().equals("ENDFOR")) {
                return i;
            }
        }
        throw new ZpmRuntimeException("FOR without ENDFOR at", startLineNum);
    }

    private static void processPrintStatement(String line, int lineNum) throws ZpmRuntimeException {
        String varName = line.substring(6).replace(";", "").trim();
        if (!variables.containsKey(varName)) {
            throw new ZpmRuntimeException("RUNTIME ERROR", lineNum);
        }
        System.out.println(varName + "=" + variables.get(varName));
    }
}
