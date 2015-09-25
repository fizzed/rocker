Rocker Templates by Fizzed
=======================================

 - [Fizzed, Inc.](http://fizzed.com)
 - Joe Lauer (Twitter: [@jjlauer](http://twitter.com/jjlauer))

## Syntax

Rocker uses an intuitive, **tagless** syntax with standard Java expressions for
iteration, control, and values.  Templates consist of two parts - plain static
text and dynamic code.  Rocker's parser considers everything to be plain text
by default, but switches to dynamic mode when it sees the ```@``` character.  Rocker
infers when your dynamic code ends by tapping into Java's own rules for variable
names, methods, and general structure. The only other special characters are
curly brackets -- which are used to indicate the start and end of blocks. As long
as you make sure a left curly has a matching right curly, the only character
you will need to escape in your plain text is the @ char.

## Using literals @, }, and {

Since the ```@``` character indicates the start of dynamic code, to include it in your
plain text, simply escape it using ```@@```. If you find the need to escape the ```{``` or ```}```
characters, escape them using ```@{``` and ```@}```.  However, as long as you make sure your
left curly has a matching right curly, you will rarely ever need to escape them.

## Template Preamble

Each template starts with an optional preamble.  The preamble is where you define
Java imports, options, and arguments.  All of these are optional.  Your template
body is considered to begin after the last preamble statement.

### Import declarations

These are standard Java import statements that are added to the top of generated
Java source code.  These start with @import and end with a newline (do not end
them with a semi-colon).  Both static and wildcard imports are supported. Since
these are declared at the top of generated Java code, they apply to the template
arguments declaration for resolution.  Also, remember that other templates
are also placed into Java packages, so you can import other templates
if you want to shorten their syntax.

    @import java.util.*
    @import static java.lang.Math.*
    @import views.CommonPage

    @args (Map<String,String> map)

    @CommonPage.template()

### Option declarations

Options control how templates are parsed and generated. These normally
should be globally set (e.g. in the maven plugin configuration), but there are times
when setting them per-template is useful.  Any option can be set since the
option statement accepts a key=value syntax.  Here is a sample disabling
the discardLogicWhitespace and setting the target charset to something other
than the global default.

    @option discardLogicWhitespace=false
    @option targetCharset=UTF-16

    Hello!

A full list of options is below.

### Arguments declaration

A single arguments statement declares the parameters to the template. If this
statement is not included, then the template has no arguments.  However, we
generally find it's still helpful to include an empty list for readability sake.

    @import java.util.Map
    @import java.util.Date

    @args (Map<String,String> map, Date d)

    Hello!

## Template Body

The template body begins after the last preamble statement.  The body includes
a mix of plain static text and dynamic code.

### Value expressions

Outputs the value during the render. Rocker does not require you to explicitly
provide an end tag, the end of the expression is inferred from standard Java
constraints on variable/method names.  Let's say you have a User object with
a String property getName() that returns "John Smith".

    @args (User user)

    Name: @user.getName().substring(0, 4)

The value expression ```@user.getName().substring(0, 4)``` will return ```John```
since Rocker inferred the entire chained call was all part of the same expression.
All sorts of value expressions will work exactly as you would expect. You can
call static or instance methods, chained methods, and pass arguments.

### Automatic escaping

Rocker will automatically escape any of your dynamic content based on the
template content type. Rocker determines the content type of a template by the
file extension of the template.  A template named `index.rocker.html` will have
a content type of HTML and HTML escaping will be used for any value expression.
Let's say you supply a string of `"<html>"` as the `element` value to the following
template.

```html
@args (String element)

@element
```

Since HTML escaping is in effect the actual rendered output is

```html
&lt;html&gt;
```

For the cases where you'd like to bypass these rules and directly render your
content, use the `@raw` method.

```html
@args (String element)

@raw(element)
```

The rendered output is

```html
<html>
```

### If-else blocks

Standard Java if-else control flow.  The left curly character ```{``` indicates
the start of the block and the right curly character ```}``` marks the end.

    @if (booleanVar) {
        i am in if block!
    } else {
        i am in else block!
    }
    
Note that Rocker has intelligence to skip template content that includes ```{```
and ```}``` characters such as JavaScript or CSS.  You will not need to escape
these characters as long as you have matching left and right curly brackets.

### Iteration

Standard Java for loops as well as enhanced syntax for Java 6 & 7 and
fancier syntax for Java 8 (where types are inferred).  Since iteration is such
an important part of rendering templates, Rocker provides support for a special
```ForIterator``` object that can be included with for-loops.

#### Rocker iterator

If you request an iterator, the index of current item

    @i.index()

If the item is the first

    @i.first()

If the item is the last
    
    @i.last()

#### Standard for-loop

    @for (int i = 0; i < items.size(); i++) {
        Item: @items.get(i)
    }

#### Enhanced for-loop with Collection

For Java 8 (note this is statically typed and checked at compile time)

    @for (item : items) {
        Item: @item
    }

And for Java 6/7

    @for (String item : items) {
        Item: @item
    }

#### Enhanced for-loop with Collection and Rocker iterator support

For Java 8 (note this is statically typed and checked at compile time)

    @for ((i, item) : items) {
        Item @i.index() = @item
    }

And for Java 6/7

    @for ((ForIterator i, String item) : items) {
        Item @i.index() = @item
    }

#### Enhanced for-loop with Map

For Java 8 (note this is statically typed and checked at compile time)

    @for ((key, item) : itemMap) {
        @key = @item
    }

And for Java 6/7

    @for ((String key, String item) : itemMap) {
        @key = @item
    }

#### Enhanced for-loop with Map and Rocker iterator support

For Java 8 (note this is statically typed and checked at compile time)

    @for ((i, key, item) : itemMap) {
        @key = @item (at index @i.index())
    }

And for Java 6/7

    @for ((ForIterator i, String key, String item) : itemMap) {
        @key = @item (at index @i.index())
    }

### Calling other templates

Templates can include other templates during its rendering process similar to
how you use them from Java. Other templates are resolved as normal classes
(e.g. packageName.className syntax).

A template can include another template

    @views.MyOther.template()

If that template requires arguments, then you simply pass them along

    @views.MyOther.template(myArg1, "Another arg")

If that template requires a ```RockerBody``` argument (more on this later), then
you can include it using a closure syntax after the template value expression

    @views.MyOther.template(myArg1, "Another arg") -> {
        i will be rendered in MyOther
    }

Or as of v0.10.0 call another template dynamically (by name, not class)

    @import com.fizzed.rocker.Rocker

    @Rocker("views/MyOther.rocker.html", "myArg1", "Another arg")

### Content blocks

Sometimes its useful to assign a block of content to a variable that can be
called in other places or even passed along to another template.

    @var => {
        content block
    }

    Hello, i am in the @var 

That template would render ```Hello, i am in the content block```.  Templates
can pass these to each other by declaring an argument of type ```RockerContent```.

    @args (RockerContent content)

    @content

### RockerBody

A ```RockerBody``` is a special type of ```RockerContent``` that let's a template
declare that it expects a content block passed to it when its included in
other templates.  The ```RockerBody``` must be the last variable declared in ```@args```.
While it functions identically to ```RockerContent```, it helps support a more
intuitive syntax for passing around the "body" of templates.  Let's say you
create a template ```views.Hello```

    @args (RockerBody body)
    
    Header
    @body
    Footer

Then in another template that includes it

    @args ()

    @views.Hello.template() -> {
        i am in the body
    }

This would render

    Header
        i am in the body
    Footer

## Comments

Server side comments can be used anywhere (preamble or body)

    @*
        I am a comment
    *@

## Options

The following options are supported in the preamble option statement.

### Discard logic whitespace

    @option discardLogicWhitespace=true

Helps make your template output look more professional by carefully removing
whitespace, but only in some circumstances.  This value is only enabled by
default for certain content types such as HTML.  The algorithm is as follows:

 - Discard any lines that are only whitespace up till the first line of non-whitespace
   text or an expression that would output a value.

 - Then discard lines consisting entirely of block-level logic such as if/else
   blocks, or content/value closures.  Lines that mix non-whitespace text and
   block-level logic will be skipped.

Without this feature enabled, Rocker and many other template engines render
HTML that includes blank lines due to server-side logic. The following is pulled
from twitter.com. Notice all the blank lines?  Clearly twitter has lots of 
server-side logic in those places.

```html


<!DOCTYPE html>
  <head>
    

    <meta charset="utf-8">
```

With this feature enabled, Rocker can get you close to this:

```html
<!DOCTYPE html>
  <head>
    <meta charset="utf-8">
```

This feature's goal is to not remove all whitespace, just the whitespace added
due to server-side logic that unfortunately adds extra newlines you really didn't
want rendered.

### Java version

    @option javaVersion=1.8

The Java version you'd like your templates be compile & runtime compatible with.
Defaults to the Java version of the JVM executing the parser (e.g. "1.8").
 
### Extends class

    @option extendsClass=com.sample.MyAppBaseTemplate

The class that all templates should extend. Useful for application-specific
intermediate classes that you'd like all templates to extend. Defaults to ```com.fizzed.rocker.RockerTemplate```

### Target charset

    @option targetCharset=UTF-8

The target charset for template output.  Defaults to UTF-8.  Accepts any value
that is supported by Java's standard charsetName.

## Handling null values

We suggest using the ```Optional``` class either via Google Gauva for Java 6/7
or the standard class in Java 8.  With Rocker's syntax for value expressions,
the Java 8 optional prevents a NullPointerException as well as being readable.

    @Optional.ofNullable(myVar).orElse("None")

And if you don't like to type `Optional.ofNullable` -- consider using it in your 
objects as the return value.  If your object returns an `Optional`, then the syntax
is even more simplified

    @myVar.orElse("None")

## More functions please!

Rocker supports standard or wildcard Java imports via the `@import` declaration.
It also supports Java's static import -- which can be carefully used to make
other functions look like Rocker functions.  The following template makes
`java.lang.System.currentTimeMillis()` look like a Rocker template function

```html
@import static java.lang.System.currentTimeMillis

Current time in millis is @currentTimeMillis()
```

Use this technique sparingly since naming collisions of current or future
Rocker built-in features could occur. However, its a handy feature in some
circumstances.

## Building complex templates

Combining content chunks along with a RockerBody allow building complex templates
for rendering advanced content.  Here is an example demonstrating how a master
`main` template can be used to construct various sections.  Create a `views/main.rocker.html`
template.

```html
@args (String title, RockerContent extracss, RockerContent extrajs, RockerBody content)

<html>
<head>
    <title>@title</title>
    <link rel="stylesheet" href='/assets/css/bootstrap.min.css' >
    @extracss
    <body>
       @content
    <script type="text/javascript" src='/assets/js/jquery-1.10.2.min.js'></script>
    @extrajs
</body>
</html>
```

This master template can be used in a calling template. Create a `views/index.rocker.html`
template.
    
```html
@args ()

@extracss -> {
    <link rel="stylesheet" href='/assets/css/main.css' >
}

@views.main.template("Home", extracss, RockerContent.NONE) -> {
    <h1>Hello!</h1>
}
```

This will result in rendering

```html
<html>
<head>
    <title>Home</title>
    <link rel="stylesheet" href='/assets/css/bootstrap.min.css' >
    <link rel="stylesheet" href='/assets/css/main.css' >
    <body>
       <h1>Hello!</h1>
    <script type="text/javascript" src='/assets/js/jquery-1.10.2.min.js'></script>
   
</body>
</html>
```
