package io.spring.batch.helloworld.config.chapter7.custom;

import io.spring.batch.helloworld.config.chapter7.cursor.Customer;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomerItemReader extends ItemStreamSupport implements ItemReader<Customer> {
    private List<Customer> customers;
    private int curIndex;
    private String INDEX_KEY = "current.index.customers";

    private String[] firstNames = {"Michael", "Warren", "Ann", "Terrence", "Erica", "Laura", "Steve", "Larry"};
    private String middleInitial = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private String[] lastNames = {"Gates", "Darrow", "Donnelly", "Jobs", "Buffett"};
    private String[] streets = {"4th Street", "Wall Street", "Fifth Avenue", "Mt. Lee Drive"};
    private String[] cities = {"Chicago", "New York", "Hollywood", "Aurora"};
    private String[] states = {"IL", "NY", "CA", "NE"};
    private Random generator = new Random();

    public CustomerItemReader() {
        curIndex = 0;

        customers = new ArrayList<>();

        for(int i=0; i<100; i++) {
            customers.add(buildCustomer());
        }
    }

    private Customer buildCustomer() {
        Customer customer = new Customer();

        customer.setId((long) generator.nextInt(Integer.MAX_VALUE));
        customer.setFirstName(firstNames[generator.nextInt(firstNames.length - 1)]);
        customer.setMiddleInitial(String.valueOf(middleInitial.charAt(generator.nextInt(middleInitial.length() - 1))));
        customer.setLastName(lastNames[generator.nextInt(lastNames.length-1)]);
        customer.setAddress(generator.nextInt(9999) + " " + streets[generator.nextInt(streets.length-1)]);
        customer.setCity(cities[generator.nextInt(cities.length-1)]);
        customer.setState(states[generator.nextInt(states.length-1)]);
        customer.setZipCode(String.valueOf(generator.nextInt(99999)));

        return customer;
    }

    @Override
    public Customer read() {
        Customer customer = null;

        if (curIndex == 50) {
            throw new RuntimeException("This will end your execution");
        }

        if (curIndex < customers.size()) {
            customer = customers.get(curIndex);
            curIndex++;
        }

        return customer;
    }

    @Override
    public void open(ExecutionContext context) throws ItemStreamException {
        if(context.containsKey(getExecutionContextKey(INDEX_KEY))) {
            int index = context.getInt(getExecutionContextKey(INDEX_KEY));

            // 복원하려는 인덱스가 50이면 건너뛴다.
            if (index == 50) {
                curIndex = 51;
            } else {
                curIndex = index;
            }
        } else {
            curIndex = 0;
        }
    }

    @Override
    public void update(ExecutionContext context) throws ItemStreamException {
        context.putInt(getExecutionContextKey(INDEX_KEY), curIndex);
    }
}
