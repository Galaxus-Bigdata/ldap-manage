import org.kitesdk.morphline.api.Command;
import org.kitesdk.morphline.api.MorphlineContext;
import org.kitesdk.morphline.api.Record;
import org.kitesdk.morphline.base.Compiler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
        Collection<Record> outputRecords = new ArrayList<>();
        boolean success = morphline.process(record, outputRecords);
        if (!success) {
            System.out.println("Failed to process record");
        } else {
            for (Record outputRecord : outputRecords) {
                System.out.println(outputRecord);
            }
        }
    }
}
