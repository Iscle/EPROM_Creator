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
        int recordType = 0;
        int[] data = new int[16];
        int checksum = 0;

        double totalLength = Math.pow(2, 15);

        for (int address = 0; address < totalLength; address += 16) {
            checksum = 0;

            for (int j = 0; j < 16; j++) {
                int result;
                data[j] = 0;

                result = (address + j) & 0x3ff;

                if (result > 99) {
                    result = 0;
                }

                if (getBit(address + j, 13) == 0) {
                    if (getBit(address + j, 14) == 0) {
                        result = result % 10;
                    } else {
                        result = result / 10;
                    }
                } else {
                    if (getBit(address + j, 14) == 0) {
                        result = result % 16;
                    } else {
                        result = result / 16;
                    }
                }

                switch (result) {
                    case 0:
                        data[j] = setBit(data[j], 2); // n
                        data[j] = setBit(data[j], 6); // t
                        break;
                    case 1:
                        data[j] = setBit(data[j], 2); // n
                        break;
                    case 2:
                        data[j] = setBit(data[j], 3); // p
                        data[j] = setBit(data[j], 7); // u
                        break;
                    case 3:
                        data[j] = setBit(data[j], 3); // p
                        break;
                    case 4:
                        data[j] = setBit(data[j], 3); // p
                        data[j] = setBit(data[j], 7); // u
                        break;
                    case 5:
                        data[j] = setBit(data[j], 3); // p
                        data[j] = setBit(data[j], 7); // u
                        break;
                    case 6:
                        data[j] = setBit(data[j], 3); // p
                        data[j] = setBit(data[j], 7); // u
                        break;
                    case 7:

                        break;
                    case 8:
                        data[j] = setBit(data[j], 3); // p
                        data[j] = setBit(data[j], 7); // u
                        break;
                    case 9:
                        data[j] = setBit(data[j], 3); // p
                        data[j] = setBit(data[j], 7); // u
                        break;
                    case 10:
                        data[j] = setBit(data[j], 3); // p
                        data[j] = setBit(data[j], 7); // u
                        break;
                    case 11:
                        data[j] = setBit(data[j], 1); // m
                        data[j] = setBit(data[j], 3); // p
                        data[j] = setBit(data[j], 5); // s
                        break;
                    case 12:

                        break;
                    case 13:
                        data[j] = setBit(data[j], 1); // m
                        data[j] = setBit(data[j], 5); // s
                        break;
                    case 14:
                        data[j] = setBit(data[j], 7); // u
                        break;
                    case 15:
                        data[j] = setBit(data[j], 7); // u
                        break;

                }

                data[j] = (~data[j]) & 0xff;

                checksum = checksum + data[j];
                System.out.println(String.valueOf((address + j) & 0x3ff) + " / " + String.valueOf(((address + j) >> 10) & 0xf) + " = " + result);
            }

            checksum = toCa2Byte(byteCount + addAddress(address) + recordType + checksum);
            array.add(":" + toHexString(byteCount, 2) + toHexString(address, 4) + toHexString(recordType, 2) + dataToString(data) + toHexString(checksum, 2));
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

    private static int getBit(int variable, int bit) {
        return (variable >> bit) & 0b1;
    }

    private static int setBit(int variable, int bit) {
        return variable | (1 << bit);
    }
}
