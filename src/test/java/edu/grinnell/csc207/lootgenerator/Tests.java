package edu.grinnell.csc207.lootgenerator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.regex.*;

/**
 * Integration tests for the LootGenerator application.
 */
public class Tests {

    /**
     * Runs the main method with a single 'n' input to exit immediately,
     * and verifies that the output contains the expected loot generation format.
     *
     * @throws Exception if an error occurs during IO or main execution
     */
    @Test
    public void testMainProducesLootFormat() throws Exception {
        // Backup original System.in and System.out
        InputStream sysInBackup = System.in;
        PrintStream sysOutBackup = System.out;

        // Prepare simulated user input: 'n' to quit after first round
        ByteArrayInputStream inContent = new ByteArrayInputStream("n\n".getBytes());
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();

        System.setIn(inContent);
        System.setOut(new PrintStream(outContent));

        // Execute main
        LootGenerator.main(new String[]{});

        // Restore original streams
        System.setIn(sysInBackup);
        System.setOut(sysOutBackup);

        String output = outContent.toString();

        // Check greeting
        assertTrue(output.contains("This program kills monsters and generates loot!"),
            "Greeting line missing");

        // Check structure: Fighting <name>... You have slain <name>! <name> dropped:
        Pattern lootSection = Pattern.compile(
            "Fighting .+\\r?\\nYou have slain .+!\\r?\\n.+ dropped:",
            Pattern.MULTILINE);
        assertTrue(lootSection.matcher(output).find(),
            "Loot section header not found");

        // Check for Defense line with a numeric value
        assertTrue(output.matches("(?s).*Defense: \\d+.*"),
            "Defense line missing or malformed");
    }
}
