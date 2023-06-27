import annotation.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DITest {

    @Test
    public void simpleTest() throws Exception {
        ClassOne classOne = DI.getInstance().get(ClassOne.class);
        Assertions.assertEquals(classOne.getService1Value(), "Service1");
        Assertions.assertEquals(classOne.getService2Value(), "Service2");
    }

    private class ClassOne {
        @Inject(type = Service1.class)
        private Service service1;

        @Inject(type = Service2.class)
        private Service service2;

        public ClassOne(){
        }

        public String getService1Value() {
            return service1.getServiceValue();
        }

        public String getService2Value() {
            return service2.getServiceValue();
        }

    }

    private interface Service {
        String getServiceValue();
    }

    private class Service1 implements Service {

        public Service1(){
        }

        @Override
        public String getServiceValue() {
            return "Service1";
        }
    }

    private class Service2 implements Service {

        public Service2(){
        }

        @Override
        public String getServiceValue() {
            return "Service2";
        }
    }
}
