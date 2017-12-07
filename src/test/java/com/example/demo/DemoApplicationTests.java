package com.example.demo;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
public class DemoApplicationTests {

    private static final String LOGGER_NAME = "TEMPLATE_CACHE";

    @SuppressWarnings("unchecked")
    private final Appender<ILoggingEvent> mockAppender = mock(Appender.class);

    @Autowired
    private MockMvc mvc;

    @Before
    public void setup() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = loggerContext.getLogger(LOGGER_NAME);
        when(mockAppender.getName()).thenReturn("MOCK");
        logger.addAppender(mockAppender);
    }

    @Test
    public void testIfFIFO() throws Exception {
        mvc.perform(get("/page1"));
        assertLogMessage("CACHE_ADD", "page1");

        mvc.perform(get("/page2"));
        assertLogMessage("CACHE_ADD", "page2");

        mvc.perform(get("/page3"));
        assertLogMessage("CACHE_ADD", "page3");

        mvc.perform(get("/page1"));
        mvc.perform(get("/page1"));
        mvc.perform(get("/page1"));

        mvc.perform(get("/page4"));
        assertLogMessage("CACHE_REMOVE", "page1");
        assertLogMessage("CACHE_ADD", "page4");
    }

    @Test
    public void testIfLRU() throws Exception {
        mvc.perform(get("/page1"));
        assertLogMessage("CACHE_ADD", "page1");

        mvc.perform(get("/page2"));
        assertLogMessage("CACHE_ADD", "page2");

        mvc.perform(get("/page3"));
        assertLogMessage("CACHE_ADD", "page3");

        mvc.perform(get("/page1"));
        mvc.perform(get("/page1"));
        mvc.perform(get("/page1"));

        mvc.perform(get("/page4"));
        assertLogMessage("CACHE_REMOVE", "page2");
        assertLogMessage("CACHE_ADD", "page4");
    }

    private void assertLogMessage(String action, String page) {
        verify(mockAppender).doAppend(argThat(new ArgumentMatcher<LoggingEvent>() {
            @Override
            public boolean matches(Object argument) {
                String message = ((LoggingEvent) argument).getFormattedMessage();
                return message.contains(action) && message.contains(page);
            }
        }));
    }
}
