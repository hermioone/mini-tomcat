package test;

import lombok.extern.slf4j.Slf4j;
import org.hermione.minit.ContainerEvent;
import org.hermione.minit.ContainerListener;

@Slf4j
public class TestListener implements ContainerListener {
    @Override
    public void containerEvent(ContainerEvent event) {
        log.info(event.toString());
    }
}