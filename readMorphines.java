import org.kitesdk.morphline.api.Command;
import org.kitesdk.morphline.api.MorphlineContext;
import org.kitesdk.morphline.api.Record;
import org.kitesdk.morphline.base.Compiler;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class MorphlineExample {

    public static void main(String[] args) throws IOException {
        File morphlineFile = new File("path/to/your/morphline/file.conf");
        MorphlineContext morphlineContext = new MorphlineContext.Builder().build();
        Command morphline = new Compiler().compile(morphlineFile, null, morphlineContext, null);

        // Prepare a sample record
        Record record = new Record();
        record.put("yourFieldName", "yourData");

        // Process the record
        try {
            morphline.process(record);
            Collection<Record> outputRecords = morphlineContext.getFinalRecords();
            for (Record outputRecord : outputRecords) {
                System.out.println(outputRecord);
            }
        } catch (Exception e) {
            System.out.println("Failed to process record: " + e.getMessage());
        }
    }
}
