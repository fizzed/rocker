parser:
	mvn -pl compiler -am test -Pexec-compiler -DskipTests=true -Dexec.classpathScope="test" -Dexec.mainClass="com.fizzed.rocker.compiler.ParserMain"

generator:
	mvn -pl compiler -am test -Pexec-compiler -DskipTests=true -Dexec.classpathScope="test" -Dexec.mainClass="com.fizzed.rocker.compiler.GeneratorMain"

render:
	mvn -pl java6test -am test -Pexec-java6test -DskipTests=true -Dexec.classpathScope="test" -Dexec.mainClass="com.fizzed.rocker.RenderMain"

java8:
	mvn -pl java6test -am test -Pexec-java6test -DskipTests=true -Dexec.classpathScope="test" -Dexec.mainClass="com.fizzed.rocker.Java8Main"

netty:
	mvn -pl java6test -am test -Pexec-java6test -DskipTests=true -Dexec.classpathScope="test" -Dexec.mainClass="com.fizzed.rocker.NettyMain"

constant:
	mvn -pl java6test -am test -Pexec-exec-java6test -DskipTests=true -Dexec.classpathScope="test" -Dexec.args="-cp %classpath com.fizzed.rocker.ConstantPoolMain"