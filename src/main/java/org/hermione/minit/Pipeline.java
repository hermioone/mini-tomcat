package org.hermione.minit;

import java.io.IOException;
import javax.servlet.ServletException;

/**
 * Pipeline 属于 Container，用来管理 Container 中的 Valve 链条，其中有特殊的 basic。Pipeline
 * 通过 pipeline#invoke 来启动 Valve 链条的调用。
 */
public interface Pipeline {

    /**
     * 获取 pipeline 所属的 Container
     * @return container
     */
    Container getContainer();

    Valve getBasic();

    void setBasic(Valve valve);

    void addValve(Valve valve);

    void invoke(Request request, Response response) throws IOException, ServletException;

    void removeValve(Valve valve);

}
