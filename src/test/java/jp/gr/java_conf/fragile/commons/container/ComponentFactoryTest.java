/**
 *
 */
package jp.gr.java_conf.fragile.commons.container;

import jp.gr.java_conf.fragile.commons.aspect.Interceptor;

import org.junit.Test;

/**
 * @author syuu256/gmail.com
 *
 */
public class ComponentFactoryTest {

    /**
     */
    @Test
    public void testGetComponentComponentNameable() {

        FooMock foo = ComponentFactory.createFactory(FooMock.class).getComponent(BeanNames.FOO_MOCK);
        String ret1 = foo.execute("010101", Long.valueOf(65535L));
        System.out.println(ret1);

        foo.executeEx();

    }
}

enum BeanNames implements ComponentNameable {
    FOO_MOCK;
    public String getLabel() {
        return this.name();
    }
}
interface FooMock {
    String execute(String foo, Long bar);
    String executeEx();
}
class FooMockImpl implements FooMock {
    private BarMock barMock = null;
    public void setBarMock(BarMock barMock) {
        this.barMock = barMock;
    }
    public String execute(String foo, Long bar) {
        System.out.println("execute:" + foo + ":" + bar + ":" + barMock);
        return "hoge";
    }
    public String executeEx() {
        System.out.println("executeEx:");
        return "executeEx";
    }
}

interface BarMock {
}
class BarMockImpl implements BarMock {
}
class InterceptorImpl implements Interceptor {
    public Object[] before(final Object instance, final Object[] param) {
        System.out.println("before:" + param);
        return param;
    }
    public Object after(final Object instance, final Object returnValue) {
        System.out.println("after:" + returnValue);
        return returnValue;
    }

}