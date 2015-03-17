Java constant pool for classes
=======================================

When you include a String literal in a Java source file, it is compiled and
included in the final .class file.  When the JVM loads your .class, it will
load all String literals into memory (heap in recent JVMs, or permgen in
previous JVMs).  Based on our research, these Strings literal constants will
usually stick around in memory if your class is still loaded in the ClassLoader.

If a template would like to pre-convert these Strings into byte arrays, and then
only use the byte arrays, there is no way to tell the JVM its okay to unload
the String literals from memory.  This means Rocker is left with two choices:

 - Load all plain text from resource files
 - Use a temporary classloader to use the Strings in a .class file to bootstrap
   the interal byte arrays.

Loading from resource files is fine, but it also means more configuration setup
by users of Rocker in their build tools.  Based on our research, using a temporary
classloader to bootstrap the byte arrays is almost identical to using a resource
file -- and users don't need to worry about copying resources around.

## ConstantPoolMain in java6test module

String length of 6899 characters as static final field in Strings class

When loading class normally, the following happens:
  Java ClassLoader defines class, String is a "constant"
    6899 bytes used
  Constant converted into String, interned onto heap:
    13798 bytes (2 bytes for each char)

That's why ~21K bytes are used to load byte[], but after GC then
the constant is at least GC'ed leaving ~14K bytes on heap that will
stay there forever.

Alternatively, using a temporary classloader allows the class to
still define the variable, but the class itself to be GC'ed and the
internal String constant to not be interned.  After GC, just some 
small overhead bytes are used.

All the following measurements are:

```
Bytes in memory before
> Bytes in memory after String -> byte[] load
  > Bytes in memory after byte[] is null'ed and System.gc() called
```

### Load byte array directly from resource

jdk1.8.0_31: directly from file (control to experiment!)

```
2950280
> 2957904      (7624 bytes)
  > 2951056    (776 bytes)
```

## Load byte array from temporary classloader

jdk1.8.0_31: with GC'ed classloader to load constant strings -> bytes...

```
2949008
> 2956936      (7928 bytes)    (for byte length of 6899, 2977712 while in class load = 27248 bytes)
  > 2949792    (784 bytes)
```

jdk1.7.0_75: with GC'ed classloader to load constant strings -> bytes...

```
2904808        
> 2913176      (8368 bytes)    (for byte length of 6899)
  > 2906040    (1232 bytes)
```

## Load byte arrays from static String field

jdk1.8.0_31: with use of static text field -> bytes...

```
2949000
> 2971176      (22176 bytes)   (for byte length of 6899)
  > 2964032    (15032 bytes)
```
jdk1.7.0_75: with use of static text field -> bytes...

```
2904816
> 2926544      (21728 bytes)
  > 2919832    (15016 bytes)
```