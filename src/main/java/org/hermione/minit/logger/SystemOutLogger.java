package org.hermione.minit.logger;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.servlet.ServletException;
import java.io.CharArrayWriter;
import java.io.PrintWriter;

@Slf4j
public class SystemOutLogger extends LoggerBase {
    protected static final String info = "org.hermione.minit.logger.SystemOutLogger/1.0";

    public void log(String msg) {
        log.info(msg);
    }

    @Override
    public void log(String msg, Throwable throwable) {
        CharArrayWriter buf = new CharArrayWriter();
        PrintWriter writer = new PrintWriter(buf);
        writer.println(msg);
        writer.println(ExceptionUtils.getStackTrace(throwable));
        log.error(buf.toString());
    }
}
