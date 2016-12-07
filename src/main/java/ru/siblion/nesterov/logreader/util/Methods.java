package ru.siblion.nesterov.logreader.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import org.w3c.dom.*;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.*;

/**
 * Created by alexander on 06.12.2016.
 */
public class Methods {
    public final static String DOMAIN_DIRECTORY = "C:\\Oracle\\Middleware\\Oracle_Home\\user_projects\\domains\\webl_domain\\";
    public final static String DOMAIN_NAME = "webl_domain";
    public static String adminServerDirectory;

    public static List<Integer> getExpressionPositions(String string, String filePath) {
        List<Integer> numbers = new ArrayList<Integer>();
        try {
            BufferedReader reader;
            String command = "findstr /n /c:" + "\"" + string + "\"" + " " + filePath ;
            Process p = Runtime.getRuntime().exec(command);

            InputStream is = p.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();
            while (line != null) {
                int i = 0;
                while (i < line.toCharArray().length && line.toCharArray()[i] != ':') i++;

                numbers.add(Integer.parseInt((line.substring(0, i))));
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return numbers;
    }

    public static String _getBlock(String filePath, int fromLine, int toLine) {
        System.out.println("getBlock(" + filePath + ", " + fromLine + ", " + toLine + ")");
        StringBuilder block = new StringBuilder();
        for (int i = fromLine; i <= toLine; i++) {
            try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
                block.append(lines.skip(i - 1).findFirst().get() + "\n");
            }  catch (IOException e) {
                e.getStackTrace();
            }
        }
        return block.toString();
    }

    public static String getBlock(String filePath, int fromLine, int toLine)  {
        System.out.println("getBlock(" + filePath + ", " + fromLine + ", " + toLine + ")");
        StringBuilder block = new StringBuilder();
        FileReader fr = null;
        LineNumberReader lnr = null;
        try {
            fr = new FileReader(filePath);
            lnr = new LineNumberReader(fr);
            for (int i = 1; i < fromLine; i++) {
                lnr.readLine();
            }
            for (int i = fromLine; i <= toLine; i++) {
                block.append(lnr.readLine()+ "\n");
            }
        } catch(Exception e){
            e.printStackTrace();
        } finally{
            if(fr!=null) {
                try {
                    fr.close();
                } catch (IOException e) {

                }
            }
            if(lnr!=null) {
                try {
                    lnr.close();
                } catch(IOException e) {

                }
            }
        }
        return block.toString();
    }

    public static Map<Integer, Integer> getRegExpBlocksPositions(List<Integer> regExpPositions, List<Integer> blockPositions) {
        Map<Integer, Integer> regExpBlocksPositions = new TreeMap<Integer, Integer>();
        int start = 0;
        int end = 0;
        for (int i = 0; i < regExpPositions.size(); i++) {
            for (int j = 0; j <blockPositions.size(); j++) {
                if (regExpPositions.get(i) >= blockPositions.get(j) && j + 1 < blockPositions.size() && regExpPositions.get(i) < blockPositions.get(j + 1)) {
                    start =  blockPositions.get(j);
                    end =  blockPositions.get(j + 1) - 1;
                    regExpBlocksPositions.put(start, end);
                    break;
                }
                if (j + 1 == blockPositions.size()) {
                    start =  blockPositions.get(j);
                    end =  blockPositions.get(j);
                    regExpBlocksPositions.put(start, end);
                }
            }
        }
        return regExpBlocksPositions;
    }

    public static List<String> getLogFilePaths(String location) {
        List<String> filePaths = new ArrayList<String>();

        String serverName = location;
        File directory = new File(DOMAIN_DIRECTORY + "servers\\" + serverName + "\\logs\\");
        if (directory.exists()) {
            for (String logFilePath : Methods.getListFilesMatching(directory, (serverName + ".log[0-9]*"))) {
                filePaths.add(logFilePath);
            }
            return filePaths;
        }
        List<String> servers = new ArrayList<String>();

        if (location == "webl_domain") {
            try {
                File configFile = new File(DOMAIN_DIRECTORY + "config\\config.xml");
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(configFile);
                doc.getDocumentElement().normalize();
                NodeList nList = doc.getElementsByTagName(("server"));
                for (int i = 0; i < nList.getLength(); i++) {
                    Node nNode = nList.item(i);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        servers.add(eElement.getElementsByTagName("name").item(0).getTextContent());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Pattern p = Pattern.compile("webl_cluster[0-9]+");
        Matcher m = p.matcher(location);
        if (m.matches()) {
            try {
                File configFile = new File(DOMAIN_DIRECTORY + "config\\config.xml");
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(configFile);
                doc.getDocumentElement().normalize();
                NodeList nList = doc.getElementsByTagName(("server"));
                for (int i = 1; i < nList.getLength(); i++) {
                    Node nNode = nList.item(i);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        if (eElement.getElementsByTagName("cluster").item(0).getTextContent().equals(location)) {
                            servers.add(eElement.getElementsByTagName("name").item(0).getTextContent());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        File serverLogDirectory;
        for (String server : servers) {
            serverLogDirectory = new File(DOMAIN_DIRECTORY + "servers\\" + server + "\\logs\\");
            String regExp = (server + ".log[0-9]*|webl_domain.log[0-9]*");
            for (String logFilePath : Methods.getListFilesMatching(serverLogDirectory, regExp)) {
                filePaths.add(logFilePath);
            }
        }
        return filePaths;
    }

    public static List<String> getListFilesMatching(File root, String regex) {
        if(!root.isDirectory()) {
            throw new IllegalArgumentException(root + " is no directory.");
        }
        final Pattern p = Pattern.compile(regex); // careful: could also throw an exception!
        File[] files = root.listFiles(new FileFilter(){
            @Override
            public boolean accept(File file) {
                return p.matcher(file.getName()).matches();
            }
        });
        List<String> filesMatching = new ArrayList<String>();
        for (File file : files) {
            filesMatching.add(file.toString());
        }
        return filesMatching;
    }

    public static XMLGregorianCalendar getDate(String dateString) {
        XMLGregorianCalendar xmlGregorianDate = new XMLGregorianCalendarImpl();
        String stringDateFormat = "dd.MM.yy, hh:mm:ss,SSS aa"; // проверить h 11/12
        SimpleDateFormat format = new SimpleDateFormat(stringDateFormat);
        Date date = new Date();
        try {
             date = format.parse(dateString);
        } catch (Exception e) {
            e.getStackTrace();
        }
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(date);
        try {
            xmlGregorianDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
        return xmlGregorianDate;
    }
}