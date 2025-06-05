package com.example.basics;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.lang.annotation.*;

@SpringBootApplication
public class BasicsApplication {
    public static void main(String[] args) {
        SpringApplication.run(BasicsApplication.class, args);
    }
}

@Component
class MyRunner implements ApplicationRunner {

    private final CustomerService customerService;

    MyRunner(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println(customerService.findById(1));
    }
}


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Controller
@interface CraftsmenService {

    /**
     * Alias for {@link Component#value}.
     */
    @AliasFor(annotation = Component.class)
    String value() default "";

}

@CraftsmenService
@Transactional
class CustomerService implements InitializingBean {

    private final TransactionTemplate template;

    CustomerService(TransactionTemplate template) {
        this.template = template;
    }

    void setFoo(int x) {
    }

    @PostConstruct
    void init() {
        System.out.println("init");
    }

    Customer findById(int id) {
        return (Customer) this.template.execute((TransactionCallback<Object>) status -> {
            System.out.println("findById: " + id + "");
            return new Customer(id, "John");
        });

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("afterPropertiesSet");
    }
}

record Customer(int id, String name) {
}

// 1. dependency injection
// 2. portable service abstractions
// 3. aspect oriented programming
// 4. autoconfiguration