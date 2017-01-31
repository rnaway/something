package ru.siblion.nesterov.logreader.core;

import ru.siblion.nesterov.logreader.type.*;
import ru.siblion.nesterov.logreader.util.MyLogger;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by alexander on 13.12.2016.
 */

/* Класс для получения лог-сообщений */
public class LogReader {

    private String string;
    private List<DateInterval> dateIntervals;
    private Set<String> logFiles;
    private String message;
    private int countMessage = 0;

    private static final Logger logger = MyLogger.getLogger();

    public LogReader(String string, List<DateInterval> dateIntervals, LocationType locationType, String location) {
        FileSearcher fileSearcher = new FileSearcher();
        logFiles = fileSearcher.getLogFiles(locationType, location);

        if (logFiles.size() == 0) {
            message = "Неверный параметр location";
            logger.log(Level.INFO, "Неверный параметр location");
        }
        // если лог-файлы отсутствуют, то можно дальнейшие операторы не выполнять!!
        this.dateIntervals = dateIntervals;
        this.string = string;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /* Метод*/
    private Map<String, List<Integer>> getPositionsOfLinesWithString(String string) {
        Map<String, List<Integer>> positions = new HashMap<>();

        if (logFiles.size() == 0) {
            return null; // если лог-файлов для поиска сообщений нет, то нет смысла искать
        }

        StringBuilder filesString = new StringBuilder();
        for (String logFile : logFiles) {
            filesString.append(logFile + " "); // возможно стоит переписать, используя StringJoiner, чтобы не было пробела в конце
        }

        String findstrCommand;
        if (logFiles.size() == 1) {
            findstrCommand = "findstr /n /r /s /c:\"" + string + "\" " + filesString;
        } else {
            findstrCommand = "findstr /n /r /c:\"" + string + "\" " + filesString; // TODO: 31.01.2017  параметр /b добавить если ищутся ####!
        }

        System.out.println(findstrCommand);
        Process findstrProcess = null;
        try {
            findstrProcess = Runtime.getRuntime().exec(findstrCommand);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (InputStream findstrProcessInputStream = findstrProcess.getInputStream();
            InputStreamReader findstrProcessInputStreamReader = new InputStreamReader(findstrProcessInputStream);
            BufferedReader reader = new BufferedReader(findstrProcessInputStreamReader)) {

            String line = reader.readLine();

            // List<Integer> linesWithStringNumbers = new ArrayList<>();
            if (logFiles.size() == 1) { /* Строка представляет собой номер строки, в которой найдено выражение, если файл один */

            }

            /* Строка представляет собой - файл:номер - если несколько файлов */
            Pattern lineNumberPattern = Pattern.compile("^.*?:(?<lineNumber>[0-9]+?):"); // TODO: 31.01.2017 Почитать про регулярные выражения
            Matcher lineNumberMatcher;

            Pattern filePattern = Pattern.compile("[^.]+\\.log\\d*"); // вернуть ^ в начало выражения
            Matcher fileMatcher;

            while (line != null) {
                lineNumberMatcher = lineNumberPattern.matcher(line);
                lineNumberMatcher.find();
                String lineNumberString = lineNumberMatcher.group("lineNumber");
                Integer lineNumber = Integer.parseInt(lineNumberString);

                fileMatcher = filePattern.matcher(line);
                fileMatcher.find();
                String currentFile = fileMatcher.group();
                put(positions, currentFile, lineNumber);

                line = reader.readLine();
            }
        } catch(IOException e) {
            logger.log(Level.SEVERE, "Ошибка при получении номеров строк в файле", e) ;
        }
        return positions;
    }

    private void put(Map<String, List<Integer>> positions, String file, int lineNumber) {
        if (positions.containsKey(file)) {
            positions.get(file).add(lineNumber);
        } else {
            List<Integer> lineNumberList = new ArrayList<>();
            lineNumberList.add(lineNumber);
            positions.put(file, lineNumberList);
        }
    }

    private Map<Integer, Integer> getBlockPositions(List<Integer> positionsOfLinesWithString,
                                                    List<Integer> prefixPositions) {
        Map<Integer, Integer> blockPositions = new LinkedHashMap<>();
        System.out.println("positionsOfLinesWithString " + positionsOfLinesWithString);
        System.out.println("prefixPositions " + prefixPositions);
        /* Либо Collection<int[]> либо Collection<List<Integer>> либо Map<Integer, Integer> */
        int start;
        int end;
        for (int i = 0; i < positionsOfLinesWithString.size(); i++) {
            for (int j = 0; j < prefixPositions.size(); j++) {
                if (positionsOfLinesWithString.get(i) >= prefixPositions.get(j)
                        && j + 1 < prefixPositions.size()
                        && positionsOfLinesWithString.get(i) < prefixPositions.get(j + 1)) {
                    start =  prefixPositions.get(j);
                    end =  prefixPositions.get(j + 1) - 1;
                    blockPositions.put(start, end);
                    System.out.println("put " + start + " " + end);
                    break;
                }
                if (j + 1 == prefixPositions.size()) { // Если дошли до конца, значит искомая строка внутри последней строки-блока
                    start =  prefixPositions.get(j);
                    end =  prefixPositions.get(j);
                    blockPositions.put(start, end);
                    System.out.println("put " + start + " " + end);
                }
            }
        }
        return blockPositions;
    }

    private List<LogMessage> getLogMessagesForLogFile(String logFile, List<DateInterval> dateIntervals, Map<Integer, Integer> blockPositions)  {
        System.out.println(logFile);
        System.out.println("blockPositions");
        for (Map.Entry<Integer, Integer> blockPosition : blockPositions.entrySet()) {
            System.out.println(blockPosition.getKey() + " " + blockPosition.getValue());
        }
        List<LogMessage> logMessages = new ArrayList<>();
        StringBuilder block = new StringBuilder();

        try(FileReader fileReader = new FileReader(logFile);
            LineNumberReader lineNumberReader = new LineNumberReader(fileReader)){
            int fromLineNumber;
            int toLineNumber;
            int previousToLineNumber = 0;
            Iterator<Map.Entry<Integer, Integer>> blockPositionIterator = blockPositions.entrySet().iterator();
            while (blockPositionIterator.hasNext()) {
                Map.Entry<Integer, Integer> blockPosition = blockPositionIterator.next();

                fromLineNumber = blockPosition.getKey();
                toLineNumber = blockPosition.getValue();
                System.out.println("fromLineNumber " + fromLineNumber);
                System.out.println("toLineNumber " + toLineNumber);

                for (int i = previousToLineNumber + 1; i < fromLineNumber; i++) {
                    lineNumberReader.readLine();
                }
                String firstBlockLine = lineNumberReader.readLine();
                System.out.println("firstBlockLine " + "_" + firstBlockLine + "_");
                XMLGregorianCalendar logMessageDate = LogMessage.parseDate(firstBlockLine);
                for (DateInterval dateInterval : dateIntervals) {
                    if (dateInterval.containsDate(logMessageDate)) {
                        block = new StringBuilder();
                        block.append(firstBlockLine + "\n");
                        for (int i = fromLineNumber + 1; i <= toLineNumber; i++) {
                            block.append(lineNumberReader.readLine() + "\n");
                        }
                        if (!blockPositionIterator.hasNext()) { // в случае последнего лог-сообщения
                            String line;
                            while((line = lineNumberReader.readLine()) != null) {
                                System.out.println("внтури");
                                System.out.println("LINE " + line);
                                block.append(line + "\n");
                            }
                        }
                        //System.out.println("block " + "{{{{" + block + "}}}}");
                        logMessages.add(new LogMessage(logMessageDate, block.toString())); // в какой момент лучше преобразовывать в String?
                        countMessage++;
                        break; // если дата лог-сообщения входит хотя бы в один интервал дат, то добавляет его и рассматриваем следующее
                    }
                }
                previousToLineNumber = toLineNumber;
            }
        } catch(IOException e){
            logger.log(Level.SEVERE, "Ошибка при парсинге блока", e) ;
        }
        System.out.println(block.toString());
        return logMessages;
    }


    public List<LogMessage> getLogMessages() throws Exception {

        Map<String, List<Integer>> prefixPositions = getPositionsOfLinesWithString("####");
        Map<String, List<Integer>> stringPositions = getPositionsOfLinesWithString(string);
        System.out.println("prefixPositions " + prefixPositions);
        System.out.println("stringPositions " + stringPositions);

        List<LogMessage> logMessageList = new ArrayList<>();
        for (String logFile : logFiles) {
            if (prefixPositions.get(logFile) == null || stringPositions.get(logFile)== null ) {
                continue; // Если лог-файл не содержит искомую строки или префиксы, то не обрабатываем его
            }
            Map<Integer, Integer> blockPositions;
            blockPositions = getBlockPositions(stringPositions.get(logFile), prefixPositions.get(logFile));
            logger.log(Level.INFO, "blockPositions " + blockPositions);
            logMessageList.addAll(getLogMessagesForLogFile(logFile, dateIntervals, blockPositions));
        }
        message = countMessage + " log messages found";
        Collections.sort(logMessageList); // попробовать другую структуру данных, где не нужно сортировать в конце!
        return logMessageList;
    }
}