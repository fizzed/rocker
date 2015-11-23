import static com.fizzed.blaze.Systems.exec;

public class blaze {

    public void run_parser() {
        exec("mvn", "-pl", "compiler", "-am", "test", "-Pexec-compiler",
            "-DskipTests=true", "-Dexec.classpathScope=test", "-Dexec.mainClass=com.fizzed.rocker.compiler.ParserMain")
            .run();
    }
    
    public void run_compile() {
        // mvn -pl compiler -am test -Pexec-compiler -DskipTests=true -Dexec.classpathScope="test" -Dexec.mainClass="com.fizzed.rocker.compiler.CompileMain"
        exec("mvn", "-pl", "compiler", "-am", "test", "-Pexec-compiler",
            "-DskipTests=true", "-Dexec.classpathScope=test", "-Dexec.mainClass=com.fizzed.rocker.compiler.CompileMain")
            .run();
    }

}
