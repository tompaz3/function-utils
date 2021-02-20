# Function Utils
Small util library with some functional style classes (usually monads).

## Features
Features delivered by this library are:
1. `Either` - monadic sum type (coproduct) containing Left | Right.
1. `FluentChain` - monadic type helper for builder-like operations. Created due to the lack of some
   sensible solution to conditionally call method chains (especially some fluent builders).
1. `Try` - monadic type allowing `try-catch` operations to be executed in a monadic way with some
   utility methods. It's execution returns a `TryResult` monad.
1. `Transactional` - monadic type wrapping transactional executions.
1. `Locker` - monadic type helping wrapping the code within a lock.

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

## Try

`Try` monad helps executing and chaining `try-catch` operations in a nice and fluent way. This monad
is _lazy_, thus no operations are executed until `.execute()` method gets called.
`Try` execution returns a `TryResult<T>` type containing either successful value of type `T` or
a `Throwable` error. 

`TryResult<T>` unlike `Try`, executes eagerly.

Example usage:

```java
UserWithAccountsAndHistory findUserWithAccountsAndHistory(UserId id) {
   TryResult<UserWithAccountsAndHistory> userWithAccountsAndHistory = Try.of(() -> userRepository.findUserById(id))
     .mapTry(userAccountRepository::fetchUserWithAccounts)
     .peek(userWithAccounts -> log.debug("User {} has {} accounts",user.getUsername(),user.getAccounts().size()))
     .flatMapTry(userAccountRepository::fetchUserWithAccountsAndHistory)
     .execute();
     
   return userWithAccountsAndHistory.onError(UserNotFoundException.class, () -> log.warn("User with id {} not found", id))
     .onError(exception -> log.error("Could not fetch user with accounts and history for user id {}", id))
     .onErrorThrow(exception -> exception instanceof HttpConnectionException 
            && ((HttpConnectionException)exception).isTimeout(), TimeoutException::new)
     .onSuccess(this::notifyUserAccountsAndHistoryAccessed)
     .fold(exception -> UserWithAccountsAndHistory.none(), Function.identity());
}
```

## Transactional

`Transactional` monad helps to execute the logic within a transaction. No logic is executed until `execute()`
method is called. `Transactional` monad is highly dependent from `Try` and `TryResult` monad.

Requires `TransactionManager` implementation which provides methods for managing transaction: `begin()`,
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

## Locker

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

### Lock

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
    private final Supplier<IMap<Object,Object>> mapSupplier;
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


[1]: https://projectlombok.org/features/Builder
[2]: https://developers.google.com/protocol-buffers/docs/javatutorial#builders
[3]: https://hazelcast.com/products/imdg/