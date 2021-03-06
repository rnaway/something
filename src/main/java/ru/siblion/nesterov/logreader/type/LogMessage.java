package ru.siblion.nesterov.logreader.type;

import ru.siblion.nesterov.logreader.util.*;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.logging.Logger;

/**
 * Created by alexander on 07.12.2016.
 */

/* Класс, инкапсулирующий лог-сообщение */
@XmlRootElement(name = "LogMessage")
public class LogMessage implements Comparable<LogMessage> {

    private XMLGregorianCalendar date;
    private String message;

    private static final Logger logger = AppLogger.getLogger();

    public LogMessage(XMLGregorianCalendar date, String message) {
        this.date = date;
        this.message = message;
    }

    public LogMessage() {}

    public XMLGregorianCalendar getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }

    public void setDate(XMLGregorianCalendar date) {
        this.date = date;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "LogMessage:" + "\n\tDate: " + this.date + "\n\tMessage: " + this.message + "\n";
    }

    @Override
    public int compareTo(LogMessage logMessage) {
        return this.date.compare(logMessage.date);
    }
}
