package aspect.logger;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.impl.MutableLogEvent;

import java.util.ArrayList;
import java.util.List;

class TestAppender extends AbstractAppender {

    private final List<LogEvent> events = new ArrayList<>();

    TestAppender() {
        super("MockedAppender", null, null);
    }

    @Override
    public void append(LogEvent event) {
        MutableLogEvent eventCopy = new MutableLogEvent();
        eventCopy.initFrom(event);
        events.add(eventCopy);
    }

    @Override
    public String getName(){
        return "TestAppender";
    }

    public List<LogEvent> getEvents() {
        return events;
    }
}
