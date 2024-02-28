package org.hermione.minit;

public interface Loader {
    Container getContainer();
    void setContainer(Container container);
    String getPath();
    void setPath(String path);
    String getDocbase();
    void setDocbase(String docbase);
    ClassLoader getClassLoader();
    String getInfo();
    void addRepository(String repository);
    String[] findRepositories();
    void start();
    void stop();
}
