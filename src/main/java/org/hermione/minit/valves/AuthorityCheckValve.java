package org.hermione.minit.valves;

import lombok.extern.slf4j.Slf4j;
import org.hermione.minit.Pipeline;
import org.hermione.minit.Request;
import org.hermione.minit.Response;
import org.hermione.minit.ValveContext;

import javax.servlet.ServletException;
import java.io.IOException;

@Slf4j
public class AuthorityCheckValve extends ValveBase {
    public AuthorityCheckValve(Pipeline pipeline) {
        super(pipeline, "AuthorityCheckValve");
    }

    @Override
    public void invoke(Request request, Response response, ValveContext context) throws IOException, ServletException {
        log.warn("Authority check pass.");
        context.invokeNext(request, response);
    }
}
