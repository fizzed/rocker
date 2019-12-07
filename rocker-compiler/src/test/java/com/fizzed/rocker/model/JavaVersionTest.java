package com.fizzed.rocker.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class JavaVersionTest {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "1.6.0_23", "1.6"}, {"1.7.0", "1.7"}, {"1.7.0_80", "1.7"}, {"1.8.0_211", "1.8"}, {"9.0.7", "9"},
                {"10.0.2", "10"}, {"11", "11"}, {"11.0.5", "11"}, {"12.0.1", "12"}, {"13.0.1", "13"}, {"14-ea", "14"}
        });
    }

    private final String javaVersion;
    private final String expectedVersion;

    public JavaVersionTest(String javaVersion, String expectedVersion) {
        this.javaVersion = javaVersion;
        this.expectedVersion = expectedVersion;
    }

    @Test
    public void shouldResolveToExpectedVersion() {
        assertEquals(expectedVersion, JavaVersion.toVersion(javaVersion));
    }

}