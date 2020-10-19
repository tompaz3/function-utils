# Function Utils
Small util library with some functional style classes (usually monads).

## Features
Features delivered by this library are:
1. `Either` - monadic sum type (coproduct) containing Left | Right. 
1. `FluentChain` - monadic type helper for builder-like operations. Created due to the lack of some
sensible solution to conditionally call method chains (especially some fluent builders).


## FluentChain
`FluentChain` was created due to the lack of sensible solution to conditionally method chains.
The inspiration to create this builder was when I had to work with [Lombok Builder][1] 
or [Google's Protocol Buffer Builders][2].

There was often a need to call some builder method on condition that source value exists etc., e.g.:
```java
Customer fetchCustomer(PersonId id) {
  Person person = personRestClient.getById(id);
  CustomerBuilder builder = Customer.builder()
    .firstName(person.getFirstName())
    .lastName(person.getLastName());
  if(person.getAddress() != null) {
    builder = builder.address(person.getAddress());
  }
  if(Period.between(person.getDateOfBirth(), LocalDate.now()).getYears() < ADULT_AGE) {
    builder = builder.minorAge();
  }
  return person.getContact()  // Optional
    .map(builder::contact)
    .orElse(builder)
    .build();
}
```

Using `FluentChain` the code above could look like this:
```java
Customer fetchCustomer(PersonId id) {
  Person person = personRestClient.getById(id);
  return FluentChain.<CustomerBuilder>ofSingle(builder -> 
    builder.firstName(person.getFirstName())
  )
  .map(builder -> builder.lastName(person.getLastName()))
  .applyIfNotNull(CustomerBuilder::address, person::getAddress)
  .applyIfPresent(CustomerBuilder::contact, person::getContact)
  .<Integer>applyIf(CustomerBuilder::minorAge)
    .value(() -> Period.between(person.getDateOfBirth(), LocalDate.now()).getYears())
    .predicate(age -> age < ADULT_AGE)
    .apply()
  .chain(Customer::builder)
  .build();
}
```

[1]: https://projectlombok.org/features/Builder
[2]: https://developers.google.com/protocol-buffers/docs/javatutorial#builders