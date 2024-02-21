package org.hermione.minit;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Valve 接口表示的 Container 中的一段用户增加的逻辑，主要就是一个 invoke 方法。
 */
public interface Valve {

    String getName();

    String getInfo();

    Pipeline getPipeline();

    void setPipeline(Pipeline pipeline);

    void invoke(Request request, Response response, ValveContext context)
            throws IOException, ServletException;
}
