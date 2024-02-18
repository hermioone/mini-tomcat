package org.hermione.server;

/**
 * 因为 HttpRequest 和 HttpResponse 会被传入 Servlet 中，如果我们在它们的实现类中封装了很多内部方法，这些内部方法也会暴露给用户
 * 因此使用门面设计模式来解决这个问题
 */
public class HttpRequestFacade {
}
