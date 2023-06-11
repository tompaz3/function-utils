# Function Utils
Small util library with some functional style classes (usually monads).

## Features

Features delivered by this library are:

1. `Either` - monadic sum type (coproduct) containing Left | Right.
1. `Try` - monadic type allowing `try-catch` operations to be executed in a monadic way with some
   utility methods.
1. `Transactional` - monadic type wrapping transactional executions.
1. `Locker` - monadic type helping wrapping the code within a lock.
1. `LinkedList` - functional linked list implementation.
1. `MergedList` - list wrapper for merged list collection (created by merging multiple other lists).
1. `CompareResult` - object handling comparing with `null` values.
1. `FluentComparable` - interface extending `java.lang.Comparable` providing convenience comparison methods.
1. `TailCall` - tail call optimisation structure (probably very **inefficient**, not yet stress
   tested). 

Experimental / unstable

1. `FluentChain` - monadic type helper for builder-like operations. Created due to the lack of some
   sensible solution to conditionally call method chains (especially some fluent builders).

### Either

`Either` monad is a typical either/maybe monad. It allows operating on conflicting values, when it's
allowed to work on either of the two.

Example usage:

```java
interface Adult

interface Kid

IdDocument getIdDocument(PersonId personId){
        Either<Kid, Adult> kidOrAdult=persons.findPerson(personId);
  return kidOrAdult.fold(
      idRepository::getKidIdDocument,
      idRepository::getAdultIdDocument
  );
}
```

```java
interface GeneralError {}
class SendMailError implements GeneralError {}
class UserNotFoundError implements GeneralError {}
class MissingUserEmailAddressError implements GeneralError {}

Either<? extends GeneralError, Void> sendMailToUser(UserId userId, Mail mail) {
  return findUser(userId) // Either<UserNotFoundError, User>
      .flatMap(this::getUserEmailAddress) // Either<MissingUserEmailAddressError, EmailAddress>
      .flatMap(emailAddress -> sendEmail(emailAddress, mail)); // Either<SendMailError, Void>
}
```

### Try

`Try` monad helps executing and chaining `try-catch` operations in a nice and fluent way. This monad
is _lazy_, thus no operations are executed until `.execute()` method gets called.
`Try` execution returns a `TryResult<T>` type containing either successful value of type `T` or
a `Throwable` error.

`TryResult<T>` unlike `Try`, executes eagerly.

Example usage:

```java
UserWithAccountsAndHistory findUserWithAccountsAndHistory(UserId id) {
  TryResult<UserWithAccountsAndHistory> userWithAccountsAndHistory =
    Try.of(() -> users.getById(id))
     .recover(UserNotFoundException.class, userNotFoundException -> Try.of(() -> userRepository.findUserById(id)))
     .mapTry(userAccountRepository::fetchUserWithAccounts)
     .filter(userWithAccounts -> !userWithAccounts.getAccounts().isEmpty(), userWithAccounts -> new UserAccountsNotFoundException(userWithAccounts.getId()))
     .peek(userWithAccounts -> log.debug("User {} has {} accounts",user.getUsername(),user.getAccounts().size()))
     .flatMapTry(userAccountRepository::fetchUserWithAccountsAndHistory)
     .execute();

        return userWithAccountsAndHistory.onError(UserNotFoundException.class,()->log.warn("User with id {} not found",id))
        .onError(exception->log.error("Could not fetch user with accounts and history for user id {}",id))
        .onErrorThrow(exception->
          exception instanceof HttpConnectionException
            &&((HttpConnectionException)exception).isTimeout(),TimeoutException::new
        )
        .onSuccess(this::notifyUserAccountsAndHistoryAccessed)
        .fold(exception->UserWithAccountsAndHistory.none(),Function.identity());
}
```

### Transactional

`Transactional` monad helps to execute the logic within a transaction. No logic is executed
until `execute()`
method is called. `Transactional` monad is highly dependent from `Try` and `TryResult` monad.

Requires `TransactionManager` implementation which provides methods for managing
transaction: `begin()`,
`commit()`, `rollback()`.

Example usage:

```java
private TransactionManager trxManager;

TryResult<UserWithAccount> openUserAccount(Username username, Account account) {
  return Transactional.ofChecked(() -> userRepository.findByUsername(username))
    .flatMapTry(id -> openAccount(user, account))
    .withManager(trxManager)
    .execute();
}

Transactional<UserWithAccount> openAccount(User user, Account account) {
  return Transactional.of(() -> accountRepository.create(user.getId(), account))
    .map(createdAccount -> UserWithAccount.of(user, createdAccount));
}
```

### Locker

`Locker` monad helps to execute the logic within a transaction. No logic is executed until `execute()`
method is called. `Locker` monad is highly dependent from `Try` and `TryResult` monad.

Requires `Lock` implementation, which has `lock()` and `unlock()` methods.

Example usage:

```java
TryResult<Account> getOrCreateUserDefaultAccount() {
   return Locker.of(() -> userRepository.findByUsername(username))
     .map(User::getId)
     .map(user -> accountService.hasNoAccountYet(user.getId()) // this check requires the lock (critical section), 
                                                               // not to create multiple deafult accounts for the user
           ? accountService.openDefaultAccount(user.getId())
           : accountService.findDefaultAccountByUserId(user.getId())
     )
     .withLock(() -> lockRegistry.usernameLock(username))
     .execute();
}
```

#### Lock

`Lock` is a simple class with `lock()` and `unlock()` methods, used by `Locker` monad.

Sample usage (with [Hazelcast IMDG][3]):
```java
public class HazelcastLockRegistry {
  private final HazelcastInstance hzInstance;
  private final HzLockConfig hzLockConfig;
  
  HazelcastLockRegistry(HazelcastInstance hzInstance, HzLockConfig hzLockConfig) {
   this.hzInstance = hzInstance;
   this.hzLockConfig = hzLockConfig;
  }

   public Lock usernameLock(String username) {
    return new Lock(() -> hzInstance.getMap(hzLockConfig.getUsernameMap()),
        username,
        hzLockConfig.getUsernameLeaseDuration());
  }

   private static class HzMapLock implements Lock {

      private final Supplier<IMap<Object, Object>> mapSupplier;
      private final Object key;
      private final Duration leaseDuration;

      @Override
      public Try<Void> lock() {
         return Try.of(mapSupplier)
                 .map(map -> map.lock(key, leaseDuration.toMillis(), TimeUnit.MILLISECONDS));
      }

      @Override
      public Try<Void> unlock() {
         return Try.of(mapSupplier)
                 .map(map -> map.unlock(key);
      }
   }
}
```

### LinkedList

`LinkedList` is a functional style immutable linked list implementation.

It implements the standard Java `java.lang.Iterable` interface.

`LinkedList` has a few static factory methods:

```java
LinkedList<String> emptyList = LinkedList.empty();

LinkedList<String> singleElementList = LinkedList.of("I am a string");
        
LinkedList<String> ofSomeIterable = LinkedList.ofAll(List.of("A", "B", "C"));

LinkedList<String> ofSomeStream == LinkedList.ofAll(Stream.of("A", "B", "C"));
```

Allows access to elements:

```java
String firstElement = linkedList.head();

Optional<String> maybeFirstElement = linkedList.headerOptional();

LinkedList<String> tail = linkedList.tail();

boolean empty = linkedList.isEmpty();

int size = linkedList.size();

boolean contains = linkedList.contains("A");
```

Allows list manipulation (creating new immutable `LinkedList`, in every case):

```java
LinkedList<String> withNewElement = linkedList.add("Z");

LinkedList<String> withOtherLinkedListElements = linkedList.add(otherLinkedList);

LinkedList<String> withOtherIterableElements = linkedList.addAll(iterable);

LinkedList<String> withoutFirstFiveElements = linkedList.traverseBackwards(5);
```

Has some Java Stream interop methods:

```java
Stream<String> stringStream = linkedList.stream();

linkedList.forEach(System.out::println);
```

### MergedList

`MergedList` is a collection extending standard Java `java.util.AbstractList`.
It is a wrapper for multiple lists merged together.

It stores given lists in an array of lists, utilising already instantiated lists, thus 
avoiding creating copies.

### CompareResult

`CompareResult` is a compare result wrapper with support for `null` values.
It allows separating `null` and `non-null` comparison.

Can be of type holding information about any compared value being `null`, 
which can be accessed by one of the methods:
* `boolean isLeftEmpty()` - `true` when only left value is `null`, `false` otherwise.
* `boolean isRightEmpty()` - `true` when only right value is `null`, `false` otherwise.
* `boolean isBothEmpty()` - `true` when both left and right are `null`, `false` otherwise.

When no value was `null` and comparison result is valid, it can be checked by the method:
* `boolean isValid()` - `true` when no value was `null` and instance holds a valid comparison result.
`false` otherwise.

Value can be accessed optionally, using the method:
* `Optional<Integer> get()` - returns empty instance when any compared value is `null` 
and non-empty instance, when both values are `non-null` and comparison is valid.

Instance also provides method for `folding` in two ways:

1\. Reacting on every instance type:
```java
public <T> T fold(
  Supplier<? extends T> onLeftEmpty,
  Supplier<? extends T> onRightEmpty,
  Supplier<? extends T> onBothEmpty,
  Function<? super Integer, ? extends T> onValid
)
```

This functions calls given `Suppliers` or `Function` based on instance type.

2\. Reacting with one `Supplier` for non-valid type and `Function` for valid type:
```java
public <T> T fold(
  Supplier<? extends T> onInvalid,
  Function<? super Integer, ? extends T> onValid
)
```

#### Use examples

```java
// file
public enum ProductPriority {
  LOWEST(4),
  LOW(3),
  MODERATE(2),
  HIGH(1),
  HIGHEST(0);
  
  private final int order;
  
  ProductPriority(int order) {
    this.order = order;
  }
  
  public int order() {
    return order;
  }
}
// ---
// file
public record Type(String name, ProductPriority priority) {}
// ---
// file
public record Product(String code, String name, Type type) {
  public boolean isHigherPriorityTypeThan(Product other) {
    return CompareResult.compare(this, other, Product::toProductPriorityComparable)
            .fold(
                    () -> false, // this has no priority
                    () -> true,  // this has priority and other has none
                    () -> false, // both have no priority
                    result -> result > 0 // both have priority - verify comparison result
            );
  }
  
  private static Integer toProductPriorityComparable(Product product) {
     return isNull(product.type())
             ? null
             : isNull(product.type())
               ? null
               : isNull(product.type().priority())
                 ? null
                 : product.type().priority().order();
  }
}
```

### FluentComparable

This interface extends standard `java.lang.Comparable` providing some convenience methods.
`FluentComparable` methods use `java.lang.Comparable#compare` method to execute comparisons
and take advantage of `java.lang.Comparable` contract specification, creating results based on comparisons to the `0` integer.

Convenience methods provided by this interface are:

1. `isGreaterThan(T other)` - to check if this object is greater than the provided `other`.
1. `isEqualTo(T other)` - to check if this object is equal to the provided `other`.
1. `isLowerThan(T other)` - to check if this object is lower than the provided `other`.
1. `isGreaterOrEqualTo(T other)` - to check if this object is greater than or equal to the provided `other`.
1. `isLowerOrEqualTo(T other)` - to check if this object is lower than or equal to the provided `other`.

### TailCall

`TailCall` is a structure allowing `StackOverflowError` avoidance in case of huge recursive calls. 
It utilises a trick of storing next execution calls as objects on Heap, 
instead of method calls on Stack.

CAUTION: May be very ineffective. Not yet stress tested.

Example usage:

```java
package acme;

import com.tp.tools.function.data.LinkedList;
import java.util.stream.Stream;

public class Main {

  public static void main(String[] args) {
    LinkedList<Integer> seq = generateHugeLinkedList();
    int lastElement = iterateToLastElementRecursively(seq);
    System.out.println(lastElement); // prints last element (1 as set by generateHugeLinkedList() method)
  }

  private static int iterateToLastElementRecursively(LinkedList<Integer> list) {
    if (list.isEmpty()) {
      return TailCall.complete(-1);
    } else if (list.size() == 1) {
      return TailCall.complete(list.head());
    } else {
      return TailCall.next(() -> iterateToLastElementRecursively(list.tail()));
    }
  }

  private static LinkedList<Integer> generateHugeLinkedList() {
    AtomicInteger counter = new AtomicInteger();
    return com.tp.tools.function.data.LinkedList.ofAll(
        Stream.generate(counter::incrementAndGet)
          .limit(100_000)
    );
  }
}
```

## Experimental Features

### FluentChain

`FluentChain` was created due to the lack of sensible solution to conditionally method chains. The
inspiration to create this builder was when I had to work with [Lombok Builder][1]
or [Google's Protocol Buffer Builders][2].

There was often a need to call some builder method on condition that source value exists etc., e.g.:

```java
Customer fetchCustomer(PersonId id){
        Person person=personRestClient.getById(id);
        CustomerBuilder builder=Customer.builder()
        .firstName(person.getFirstName())
        .lastName(person.getLastName());
        if(person.getAddress()!=null){
        builder=builder.address(person.getAddress());
        }
        if(Period.between(person.getDateOfBirth(),LocalDate.now()).getYears()<ADULT_AGE){
        builder=builder.minorAge();
        }
        return person.getContact()  // Optional
        .map(builder::contact)
        .orElse(builder)
    .build();
}
```

Using `FluentChain` the code above could look like this:

```java
Customer fetchCustomer(PersonId id){
    Person person=personRestClient.getById(id);
    return FluentChain.<CustomerBuilder>ofSingle(builder ->
      builder.firstName(person.getFirstName())
      )
      .map(builder -> builder.lastName(person.getLastName()))
      .applyIfNotNull(CustomerBuilder::address,person::getAddress)
      .applyIfPresent(CustomerBuilder::contact,person::getContact)
      .<Integer>apply(CustomerBuilder::minorAge)
      .ifValue(() -> Period.between(person.getDateOfBirth(),LocalDate.now()).getYears())
      .is(age -> age < ADULT_AGE)
      .chain(Customer::builder)
      .build();
    }
```

[1]: https://projectlombok.org/features/Builder
[2]: https://developers.google.com/protocol-buffers/docs/javatutorial#builders
[3]: https://hazelcast.com/products/imdg/