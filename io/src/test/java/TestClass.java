import aio.AioClient;
import nio.NioClient;
import org.junit.Test;

public class TestClass {


    @Test
    public  void test1() {
        System.out.println(AioClient.getTime());
    }

    @Test
    public  void testNio() throws Exception{
        NioClient.main(new String[]{});
    }
}
