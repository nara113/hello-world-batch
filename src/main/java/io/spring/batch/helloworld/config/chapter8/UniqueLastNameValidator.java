package io.spring.batch.helloworld.config.chapter8;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamSupport;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.batch.item.validator.Validator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class UniqueLastNameValidator extends ItemStreamSupport implements Validator<Customer> {
    private Set<String> lastNames = new HashSet<>();

    @Override
    public void validate(Customer customer) throws ValidationException {
        if(lastNames.contains(customer.getLastName())) {
            throw new ValidationException("Duplicate last name was found : " + customer.getLastName());
        }

        lastNames.add(customer.getLastName());
    }

    @Override
    public void open(ExecutionContext executionContext) {
        String lastName = getExecutionContextKey("lastNames");

        if (executionContext.containsKey(lastName)) {
            lastNames = (Set<String>) executionContext.get(lastName);
        }
    }

    @Override
    public void update(ExecutionContext executionContext) {
        final Iterator<String> iterator = lastNames.iterator();
        Set<String> copiedLastNames = new HashSet<>();
        while (iterator.hasNext()) {
            copiedLastNames.add(iterator.next());
        }

        executionContext.put(getExecutionContextKey("lastNames"), copiedLastNames);
    }
}
