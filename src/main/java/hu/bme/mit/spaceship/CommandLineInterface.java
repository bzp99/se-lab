package hu.bme.mit.spaceship;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Scanner;
import java.util.function.BiFunction;

/**
 * Minimal command line interface (CLI) to initialize and use spaceships.
 */
public class CommandLineInterface {
    
    private static Map<String, Handler> handlers = Map.of(
        "SHIP", CommandLineInterface::handleShip,
        "TORPEDO", CommandLineInterface::handleTorpedo,
        "EXIT", CommandLineInterface::handleExit
    );

    public static void main(String[] args) {
        run(System.in, System.out, new OptionalOutput(System.err));
    }

    /**
     * Read and handle commands from an input stream, writing output to
     * another stream.
     * 
     * The optional err stream receives output only useful in an
     * interactive session.
     * 
     * @param in The input stream to read commands from
     * @param out The output stream to write results to
     * @param err Optional stream to write interactive session feedback to
     */
    public static void run(InputStream in, OutputStream out, OptionalOutput err) {
        Context ctx = new Context();
        ctx.out = new PrintStream(out);

        err.println("Welcome to the console interface.  Available commands: " + handlers.keySet().toString());
        try (Scanner scanner = new Scanner(in)) {
            boolean shouldContinue = true;
            while (shouldContinue && scanner.hasNextLine()) {
                err.print("> ");
                shouldContinue = handle(ctx, scanner.nextLine());
            }
        }
    }

    public static void run(InputStream in, OutputStream out) {
        run(in, out, new OptionalOutput(null));
    }

    /**
     * Handle a command.
     * 
     * @param ctx The current CLI context
     * @param command The command as a list of comma-separated tokens
     * @return False if no more commands should be read and application should exit; true otherwise
     */
    private static boolean handle(Context ctx, String command) {
        String[] parts = command.split(",");
        String mainCommand = parts[0].toUpperCase();
        
        boolean shouldContinue = true;
        try {
            Handler handler = handlers.get(mainCommand);
            if (handler == null) {
                ctx.out.println("UNKNOWN COMMAND");
            } else {
                shouldContinue = handler.apply(ctx, parts);
            }
        } catch (IllegalArgumentException e) {
            ctx.out.println(e.getLocalizedMessage());
        }

        return shouldContinue;
    }
    
    /**
     * Handle the SHIP command.
     */
    private static boolean handleShip(Context ctx, String[] params) {
        if (params.length != 5) {
            throw new IllegalArgumentException("SYNTAX: SHIP,<PRI_CNT>,<PRI_FAIL_RATE>,<SEC_CNT>,<SEC_FAIL_RATE>");
        }

        ctx.ship = new GT4500(Integer.parseInt(params[1]), Double.parseDouble(params[2]), Integer.parseInt(params[3]), Double.parseDouble(params[4]));
        ctx.out.println("SUCCESS");
        return true;        
    }

    /**
     * Handle the TORPEDO command.
     */
    private static boolean handleTorpedo(Context ctx, String[] params) {
        if (ctx.ship == null) {
            throw new IllegalArgumentException("SHIP NOT INITIALIZED");
        }
        if (params.length != 2) {
            throw new IllegalArgumentException("SYNTAX: TORPEDO,<SINGLE|ALL>");
        }

        FiringMode firingMode = FiringMode.valueOf(params[1].toUpperCase());            
        boolean success = ctx.ship.fireTorpedo(firingMode);
        ctx.out.println(success ? "SUCCESS" : "FAIL");
        return true;        
    }

    /**
     * Handle the EXIT command.
     */
    private static boolean handleExit(Context ctx, String[] params) {
        return false;        
    }

    private static class Context {
        SpaceShip ship;
        PrintStream out;
    }

    private static interface Handler extends BiFunction<Context, String[], Boolean> {}

    /**
     * Rudimentary PrintStream-like interface that silently ignores
     * if the underlying PrintStream is null.
     */
    private static class OptionalOutput {

        private PrintStream out;

        OptionalOutput(OutputStream out) {
            if (out != null) {
                this.out = new PrintStream(out);
            }
        }

        public void print(String message) {
            if (out != null) {
                out.print(message);
            }
        }

        public void println(String message) {
            print(message + "\n");
        }
    }
}