+++
title = "MVEL"
description = "MVEL"
weight = 12
+++

According to the [Wikipedia entry](https://en.wikipedia.org/wiki/MVEL), the
MVFLEX Expression Language (hereafter referred as MVEL) is a hybrid, dynamic,
statically typed, embeddable expression language and runtime for the Java
platform. Originally started as a utility language for an application framework,
the project is now developed completely independently. arara relies on this
scripting language in two circumstances:

1. *Rules*, as nominal attributes gathered from directives are used to build
  complex command invocations and additional computations. A rule follows a very
  strict model, detailed in [Concepts](/manual/concepts).

2. *Conditionals*, as logical expressions must be evaluated in order to decide
  whether and how a directive should be interpreted. Conditionals are detailed in
  [Concepts](/manual/concepts).

This chapter only covers the relevant parts of the MVEL language for a
consistent use with arara. For advanced topics, I highly recommend the official
language guide for MVEL 2.0, available online.

# Basic usage

The following primer is provided by the [official language
guide](https://mvel.documentnode.com/), almost verbatim, with a few
modifications to make it more adherent to our needs with arara. Consider the
following expression:

```java
user.name
```

In this expression, we have a single identifier `user.name`, which by itself is
a property expression, in that the only purpose of such an expression is to
extract a property out of a variable or context object, namely `user`. Property
expressions are widely used by arara, as directive parameters are converted to a
map inside the corresponding rule scope. For instance, a parameter `foo` in a
directive will be mapped as `parameters.foo` inside a rule during
interpretation. This topic is detailed in [Concepts](/manual/concepts). The
scripting language can also be used for evaluating a boolean expression:

```java
user.name == 'John Doe'
```

This expression yields a boolean result, either `true` or `false` based on a
comparison operation. Like a typical programming language, MVEL supports the
full gamut of operator precedence rules, including the ability to use bracketing
to control execution order:

```java
(user.name == 'John Doe') && ((x * 2) - 1) > 20
```

You may write scripts with an arbitrary number of statements using a semicolon
to denote the termination of a statement. This is required in all cases except
in cases where there is only one statement, or for the last statement in a
script:

```java
statement1; statement2; statement3
```

It is important to observe that MVEL expressions use a *last value out*
principle. This means, that although MVEL supports the `return` keyword, it can
be safely omitted. For example:

```java
foo = 10;
bar = (foo = foo * 2) + 10;
foo;
```

In this particular example, the expression automatically returns the value of
`foo` as it is the last value of the expression. It is functionally identical
to:

```java
foo = 10;
bar = (foo = foo * 2) + 10;
return foo;
```

Personally, I like to explicitly add a `return` statement, as it provides a
visual indication of the expression exit point. All rules released with arara
favour this writing style. However, feel free to choose any writing style you
want, as long as the resulting code is consistent.

The type coercion system of MVEL is applied in cases where two incomparable
types are presented by attempting to coerce the right value to that of the type
of the left value, and then vice-versa. For example:

```java
"123" == 123;
```

Surprisingly, the evaluation of such expression holds `true` in MVEL because the
underlying type coercion system will coerce the untyped number `123` to a string
`123` in order to perform the comparison.

# Inline lists, maps and arrays

According to the documentation, MVEL allows you to express lists, maps and
arrays using simple elegant syntax. Lists are expressed in the following format:

```java
[ "Jim", "Bob", "Smith" ]
```

Note that lists are denoted by comma-separated values delimited by square
brackets. Similarly, maps (sets of key/value attributes) are expressed in the
following format:

```java
[ "Foo" : "Bar", "Bar" : "Foo" ]
```

Note that attributes are composed by a key, a colon and the corresponding
value. A map is denoted by comma-separated attributes delimited  by square
brackets. Finally, arrays are expressed in the following format:

```java
{ "Jim", "Bob", "Smith" }
```

One important aspect about inline arrays is their special ability to be coerced
to other array types. When you declare an inline array, it is untyped at first
and later coerced to the type needed in context. For instance, consider the
following code, in which `sum` takes an array of integers:

```java
math.sum({ 1, 2, 3, 4 });
```

In this case, the scripting language will see that the target method accepts an
integer array and automatically type the provided untyped array as such. This is
an important feature exploited by arara when calling methods within the rule or
conditional scope.

# Property navigation

MVEL provides a single, unified syntax for accessing properties, static fields,
maps and other structures. Lists are accessed the same as arrays. For example,
these two constructs are equivalent (MVEL and Java access styles for lists and
arrays, respectively):

```java
user[5]
```

```java
user.get(5)
```

Observe that MVEL accepts plain Java methods as well. Maps are accessed in the
same way as arrays except any object can be passed as the index value. For
example, these two constructs are equivalent (MVEL and Java access styles for
maps, respectively):

```java
user["foobar"]
user.foobar
```

```java
user.get("foobar")
```

It is advisable to favour such access styles over their Java counterparts when
writing rules and conditionals for arara. The clean syntax helps to ensure more
readable code.

# Flow control

The expression language goes beyond simple evaluations. In fact, MVEL supports
an assortment of control flow operators (namely, conditionals and repetitions)
which allows advanced scripting operations. Consider this conditional statement:

```java
if (var > 0) {
   r = "greater than zero";
}
else if (var == 0) {
   r = "exactly zero";
}
else {
   r = "less than zero";
}
```

As seen in the previous code, the syntax is very similar to the ones found in
typical programming languages. MVEL also provides a shorter version, known as a
ternary statement:

```java
answer == true ? "yes" : "no";
```

The `foreach` statement accepts two parameters separated by a colon, the first
being the local variable holding the current element, and the second the
collection or array to be iterated over. For example:

```java
foreach (name : people) {
    System.out.println(name);
}
```

As expected, MVEL also implements the standard C `for` loop. Observe that newer
versions of MVEL allow an abbreviation of `foreach` to the usual `for`
statement, as syntactic sugar. In order to explicitly indicate a collection
iteration, we usually use `foreach` in the default rules for arara, but both
statements behave exactly the same from a semantic point of view.

```java
for (int i = 0; i < 100; i++) {
   System.out.println(i);
}
```

The scripting language also provides two versions of the `do` statement: one
with `while` and one with `until` (the latter being the exact inverse of the
former):

```java
do {
    x = something();
} while (x != null);
```

```java
do {
   x = something();
} until (x == null);
```

Finally, MVEL also implements the standard `while`, with the significant
addition of an `until` counterpart (for inverted logic):

```java
while (isTrue()) {
   doSomething();
}
```

```java
until (isFalse()) {
   doSomething();
}
```

Since `while` and `until` are unbounded (i.e, the number of iterations required
to solve a problem may be unpredictable), we usually tend to avoid using such
statements when writing rules for arara.

# Projections and folds

Projections are a way of representing collections. According to the official
documentation, using a very simple syntax, one can inspect very complex object
models inside collections in MVEL using the `in` operator. For example:

```java
names = (user.name in users);
```

As seen in the above code, `names` holds all values from the `name` property of
each element, represented locally by a placeholder `user`, from the collection
`users` being inspected. This feature can even perform nested operations.

# Assignments

According to the official documentation, the scripting language allows variable
assignment in expressions, either for extraction from the runtime, or for use
inside the expression. As MVEL is a dynamically typed language, there is no need
to specify a type in order to declare a new variable. However, feel free to
explicitly declare the type when desired.

```java
str = "My string";
String str = "My string";
```

Unlike Java, however, the scripting language provides automatic type conversion
(when possible) when assigning a value to a typed variable. In the following
example, an integer value is assigned to a string:

```java
String num = 1;
```

For dynamically typed variables, in order to perform a type conversion, it is
just a matter of explicitly casting the value to the desired type. In the
following example, an explicit string cast is assigned to the `num` variable:

```java
num = (String) 1;
```

When writing rules for arara, is advisable to keep variables to a minimum in
order to avoid unnecessary assignments and a potential performance
drop. However, make sure to favour readability over unmaintained code.

# Basic templating

MVEL templates are comprised of *orb* tags inside a plain text document. Orb
tags denote dynamic elements of the template which the engine will evaluate at
runtime. arara heavily relies on this concept for runtime evaluation of
conditionals and rules. For rules, we use orb tags to return either a string
from a textual template or a proper command object. The former constituted the
basis of command generation in previous versions of our tool; we highly
recommend the latter, detailed in [Concepts](/manual/concepts). Conditionals are
in fact orb tags in disguise, such that the expression (or a sequence of
expressions) is properly evaluated at runtime. Consider the following example:

```java
My favourite team is @{ person.name == 'Enrico' ? 'Juventus' : 'Palmeiras' }!
```

The above code features a basic form of orb tag named *expression orb*. It
contains an expression (or a sequence of expressions) which will be evaluated to
a certain value, as seen earlier on, when discussing the *last value out*
principle. In the example, the value to be returned will be a string containing
a football team name (the result is of course based on the comparison outcome).

# Further reading

This chapter does not cover all features of the MVEL expression language, so
further reading is advisable. I highly recommend the [MVEL language
guide](http://mvel.documentnode.com/) currently covering version 2.0 of the
language.
