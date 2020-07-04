package flashcards;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Main {
    public static Scanner scanner = new Scanner(System.in);
    public static Map<String, String> cards = new LinkedHashMap<>();
    public static ArrayList<String> log = new ArrayList<>();
    public static Map<String, Integer> errors = new LinkedHashMap<>();

    public static void main(String[] args) {
        if (isArgument("-import",args)){
            impFromFile(startFile(args));
        }

        String menu = "";
        while (!menu.equals("exit")) {
            printMsg("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):");
            menu = inputMsg().toLowerCase();
            switch (menu) {
                case "add":
                    addCard();
                    break;
                case "remove":
                    remove();
                    break;
                case "import":
                    impFromFile("");
                    break;
                case "export":
                    export("");
                    break;
                case "ask":
                    ask();
                    break;
                case "exit":
                    printMsg("Bye bye!");
                    if (isArgument("-export",args)){
                        export(exitFile(args));
                    }
                    break;
                case "log":
                    saveLog();
                    break;
                case "hardest card":
                    hardestCard();
                    break;
                case "reset stats":
                    resetStats();
                    break;
            }
        }
    }
    public static String findKey(Map<String, String> map, String value) {
        String result = "";
        for(var x : map.entrySet()) {
            if(value.equals(x.getValue())) {
                result = x.getKey();
            }
        }
        return result;
    }
    public static boolean isStringInMap(String string) {
        if (cards.containsKey(string)) {
            printMsg("The card \"" + string + "\" already exists.");
            return true;
        }
        if (cards.containsValue(string)) {
            printMsg("The definition \"" + string + "\" already exists.");
            return true;
        }
        return false;
    }
    public static void addCard() {
        String answer;
        String definition;
        printMsg("The card:");
        answer = inputMsg();
        if (!isStringInMap(answer)) {
            printMsg("The definition of card:");
            definition = inputMsg();
            if (!isStringInMap(definition)) {
                cards.put(answer, definition);
                printMsg("The pair (\"" + answer + "\":\"" + definition + "\") has been added." );
            }

        }

    }
    public static void remove() {
        printMsg("The card:");
        String cardAnswer = inputMsg();
        if (cards.containsKey(cardAnswer)) {
            cards.remove(cardAnswer);
            printMsg("The card has been removed.");
        } else {
            printMsg("Can't remove \"" + cardAnswer + "\": there is no such card.");
        }
        if (errors.containsKey(cardAnswer)) {
            errors.remove(cardAnswer);
        }
    }
    public static void impFromFile(String file) {
        int itr = 0;
        String fileName;
        if (file.length() > 0) {
            fileName = file;
        } else {
            printMsg("File name:");
            fileName = inputMsg();
        }
        try (Scanner reader = new Scanner(new File(fileName))) {
            while(reader.hasNextLine()){
                String[] line = reader.nextLine().split("\t");
                if (cards.containsKey(line[0])) {
                    cards.replace(line[0], line[1]);
                    itr++;
                } else {
                    cards.put(line[0], line[1]);
                    itr++;
                }
                if (errors.containsKey(line[0]) && line.length == 3) {
                    errors.replace(line[0], Integer.parseInt(line[2]));
                } else if (line.length == 3) {
                    errors.put(line[0], Integer.parseInt(line[2]));
                }
            }
        } catch (FileNotFoundException e) {
            printMsg("File not found.");
        }
        if (itr > 0) {
            printMsg(itr + " cards have been loaded.");
        }

    }
    public static void export(String file) {
        String fileName;
        if (file.length() > 0) {
            fileName = file;
        } else {
            printMsg("File name:");
            fileName = inputMsg();
        }
        try (PrintWriter printWriter = new PrintWriter(fileName)) {
            for (var entry : cards.entrySet()) {
                printWriter.println(entry.getKey() + "\t" + entry.getValue() + "\t" + errors.get(entry.getKey()) );
            }
        } catch (IOException e) {
            printMsg("An exception occurs " + e.getMessage());
        }
        printMsg(cards.size() + " cards have been saved.");
    }
    public static void ask() {
        Map<Integer, String> tmp = new LinkedHashMap<>();
        Set<String> keys = new LinkedHashSet<>(cards.keySet());
        int itr = 1;
        for (String x : keys) {
            tmp.put(itr++, x);
        }
        Random random = new Random(tmp.size());
        printMsg("How many times to ask?");
        int ask = Integer.parseInt(inputMsg());
        String input;
        for (int i = 0; i < ask; i++) {
            String tmpKey = tmp.get(random.nextInt((cards.size() - 1) + 1) + 1);
            printMsg("Print the definition of \"" + tmpKey + "\":");
            input = inputMsg();
            if (input.equals(cards.get(tmpKey))) {
                printMsg("Correct answer.");
            } else if (cards.containsValue(input)) {
                printMsg("Wrong answer. The correct one is \"" + cards.get(tmpKey) + "\", " +
                        "you've just written the definition of \"" + findKey(cards, input)+ "\".");
                errors.putIfAbsent(tmpKey, null);
                errors.compute(tmpKey, (k, v)-> v == null ? 1 : v + 1);
            } else {
                printMsg("Wrong answer. The correct one is \"" + cards.get(tmpKey) + "\".");
                errors.putIfAbsent(tmpKey, null);
                errors.compute(tmpKey, (k, v)-> v == null ? 1 : v + 1);
            }
        }
    }
    public static void printMsg(String string) {
        System.out.println(string);
        log.add(string);
    }
    public static String inputMsg() {
        String input = scanner.nextLine();
        log.add(input);
        return input;
    }
    public static void saveLog() {
        printMsg("File name:");
        String fileName = inputMsg();
        try (PrintWriter printWriter = new PrintWriter(fileName)) {
            for (var entry : log) {
                printWriter.println(entry);
            }
        } catch (IOException e) {
            printMsg("An exception occurs " + e.getMessage());
        }
        printMsg("The log has been saved.");

    }
    public static void hardestCard() {
        if (errors.isEmpty()) {
            printMsg("There are no cards with errors.");
        } else if (errors.size() == 1) {
            StringBuilder cardAnswer = new StringBuilder();
            for (String x : errors.keySet()) {
                cardAnswer.append(x);
            }
            Integer errorsNumber = Collections.max(errors.values());
            printMsg("The hardest card is \"" + cardAnswer + "\". You have " + errorsNumber + " errors answering it.");
        } else {
            StringBuilder cardAnswer = new StringBuilder();
            for (String x : errors.keySet()) {
                cardAnswer.append("\"").append(x).append("\", ");
            }
            cardAnswer.delete(cardAnswer.length()-2, cardAnswer.length());
            Integer errorsNumber = Collections.max(errors.values());
            printMsg("The hardest card are " + cardAnswer + ". You have " + errorsNumber + " errors answering them.");
        }
    }
    public static void resetStats() {
        errors.clear();
        printMsg("Card statistics has been reset.");
    }
    public static String startFile(String[] args) {
        String fileName ="";
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-import")) {
                fileName = args[i + 1];
            }
        }
        return fileName;
    }
    public static String exitFile(String[] args) {
        String fileName ="";
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-export")) {
                fileName = args[i + 1];
            }
        }
        return fileName;
    }
    public static boolean isArgument(String arg, String[] args) {
        boolean isArgument = false;
        for (String s : args) {
            if (s.equals(arg)) {
                isArgument = true;
                break;
            }
        }
        return isArgument;
    }

}
