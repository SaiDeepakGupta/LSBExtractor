import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class LSBExtractor {

    private static final int Skip_100_Bytes = 100;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java LSBExtractor <inputFilePath> <outputFilePath>");
            return;
        }

        String inputFilePath = args[0];
        String outputFilePath = args[1];

        try {
            int dataSize = extractBytes(inputFilePath, outputFilePath);

            if (dataSize != -1) {
                System.out.println("File Generated");
            } else {
                System.out.println("No indicator bits found");
            }

        } catch (IOException e) {
            System.err.println("Error in opening the file or writing the result_data into th file  " + e.getMessage());
        }
    }

    public static int extractBytes(String inputFilePath, String outputFilePath) throws IOException {
        try (FileInputStream inputfile = new FileInputStream(inputFilePath)) {
            long bytesSkipped = skipBytes(inputfile, Skip_100_Bytes);
            if (bytesSkipped != Skip_100_Bytes) {
                throw new IOException("Could not skip required number of bytes.");
            }

            StringBuilder LSBBytes = LSB_Bytes(inputfile);
            String lsbBytes = LSBBytes.toString();

            boolean containsSequence = findIndicatorSequence(lsbBytes);
            if (containsSequence) {
                return ExtractedData(lsbBytes, outputFilePath);
            }
            return -1;
        }
    }

    public static long skipBytes(FileInputStream inputfile, int bytesToSkip) throws IOException {
        return inputfile.skip(bytesToSkip);
    }

    public static StringBuilder LSB_Bytes(FileInputStream inputfile) throws IOException {
        StringBuilder LSBBytes = new StringBuilder();
        int currentByte;
        while ((currentByte = inputfile.read()) != -1) {
            int lsb = currentByte & 0x01;
            LSBBytes.append(lsb);
        }
        return LSBBytes;
    }

    public static boolean findIndicatorSequence(String lsbBytes) {
        StringBuilder indicatorSequenceBuilder = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            indicatorSequenceBuilder.append("10100101");
        }
        String indicatorSequence = indicatorSequenceBuilder.toString();
        return lsbBytes.contains(indicatorSequence);
    }

    public static byte[] convertBinaryStringToByteArray(String binaryData, int dataSize) {
        byte[] resultData = new byte[dataSize];
        for (int j = 0; j < dataSize; j++) {
            String byteStr = binaryData.substring(j * 8, (j + 1) * 8);
            byteStr = new StringBuilder(byteStr).reverse().toString();
            int byteInt = Integer.parseInt(byteStr, 2);
            resultData[j] = (byte) byteInt;
        }
        return resultData;
    }

    public static int ExtractedData(String lsbBytes, String outputFilePath) throws IOException {
        String StringBytes = lsbBytes.substring(64, 91);
        StringBuilder reversebytes = new StringBuilder(StringBytes).reverse();
        int dataSize = Integer.parseInt(reversebytes.toString(), 2);

        String conversionData = lsbBytes.substring(91, 91 + dataSize * 8);

        byte[] resultData = convertBinaryStringToByteArray(conversionData, dataSize);

        try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
            fos.write(resultData);
        }

        return dataSize;
    }
}
