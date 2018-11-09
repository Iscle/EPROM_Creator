package me.iscle;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Main {

    public static void main(String[] args) {
        SimpleDateFormat dateFormat;
        Date date;
        ArrayList<String> lines;
        Path file;

        dateFormat = new SimpleDateFormat("ddMMyyyy-HHmmss");
        date = new Date();
        lines = new ArrayList<String>();
        file = Paths.get("EPROM_Creator-" + dateFormat.format(date) + ".hex");

        System.out.println("Filename: " + file.toString());

        fillArray(lines);

        try {
            if (Files.exists(file)) {
                Files.delete(file);
            }
            Files.write(file, lines, Charset.forName("UTF-8"));
        } catch (IOException e) {
            System.out.println("There was an error writing the file! Aborting.");
            e.printStackTrace();
        }
    }

    private static void fillArray(ArrayList<String> array) {
        int byteCount = 16;
        int address = 0;
        int recordType = 0;
        int[] data = new int[16];
        int checksum = 0;

        double totalLength = Math.pow(2, 15);
        double combinations = Math.pow(2, 14);

        while (address < combinations) {
            checksum = 0;

            for (int i = 0; i < 16; i++) {
                int result = divide((address + i) & 0x3ff, (address + 1) >> 10);

                if (result > 99) {
                    data[i] = 0;
                    result = 0;
                } else {
                    data[i] = result;
                }

                checksum = checksum + data[i];
                System.out.println(String.valueOf((address + i) & 0x3ff) + " by " + String.valueOf((address + 1) >> 10) + " = " + result);
            }

            checksum = toCa2Byte(byteCount + addAddress(address) + recordType + checksum);
            array.add(":" + toHexString(byteCount, 2) + toHexString(address, 4) + toHexString(recordType, 2) + dataToString(data) + toHexString(checksum, 2));

            address += 16;
        }

        for (int i = 0; i < 16; i++) {
            data[i] = 0;
        }

        while (address < totalLength) {
            checksum = toCa2Byte(byteCount + addAddress(address) + recordType);
            array.add(":" + toHexString(byteCount, 2) + toHexString(address, 4) + toHexString(recordType, 2) + dataToString(data) + toHexString(checksum, 2));

            address += 16;
        }

        array.add(":00000001FF"); // End of file
    }

    private static String toHexString(int number, int length) {
        StringBuilder hexString = new StringBuilder();
        int difference;

        hexString.append(Integer.toHexString(number).toUpperCase());
        difference = length - hexString.length();

        while (difference > 0) {
            hexString.insert(0, "0");
            difference--;
        }

        return hexString.toString();
    }

    private static int toCa2Byte(int number) {
        return ((~number) + 1) & 0xFF;
    }

    private static int divide(int number, int by) {
        return by == 0 ? 0 : number / by;
    }

    private static String dataToString(int[] data) {
        StringBuilder string = new StringBuilder();

        for (int i = 0; i < data.length; i++) {
            string.append(toHexString(data[i], 2));
        }

        return string.toString();
    }

    private static int addAddress(int address) {
        return address / 256 + address % 256;
    }
}
