import org.kitesdk.morphline.api.Command;
import org.kitesdk.morphline.api.MorphlineContext;
import org.kitesdk.morphline.api.Record;
import org.kitesdk.morphline.base.Compiler;
import org.kitesdk.morphline.base.Fields;
import org.kitesdk.morphline.stdio.AbstractMorphlineTest;

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
        record.put(Fields.ATTACHMENT_BODY, "your data here");

        // Process the record
        boolean success = morphline.process(record);
        if (!success) {
            System.out.println("Failed to process record");
        } else {
            Collection<Record> outputRecords = morphlineContext.getFinalRecords();
            for (Record outputRecord : outputRecords) {
                System.out.println(outputRecord);
            }
        }
    }
}
