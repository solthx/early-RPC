import com.earlyrpc.client.config.ConsumerDescription;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.Serializable;

/**
 * @author czf
 * @Date 2020/8/15 7:23 下午
 */
public class Main {
    public static void main(String[] args) {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring.xml");
        ConsumerDescription consumerDescription = (ConsumerDescription) applicationContext.getBean("helloDesc");
        System.out.println(consumerDescription);
    }
}