package util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

public class CmdUtil
{
    public static BufferedReader getBrFromArgs(String[] args, String usage, boolean allowStdin) {
        if(args.length == 0 && allowStdin)
            return new BufferedReader(new InputStreamReader(System.in));
        else if(args.length == 1) {
            if(args[0].equals("-h") || args[0].equals("--help")) {
                System.out.println(usage);
                System.exit(0);
                return null;
            }
            else {
                try {
                    return new BufferedReader(new FileReader(args[0]));
                }
                catch(FileNotFoundException e) {
                    System.err.println(e);
                    System.err.println(usage);
                    System.exit(1);
                    return null;
                }
            }
        }
        else {
            System.err.println(usage);
            System.exit(1);
            return null;
        }
    }
}
