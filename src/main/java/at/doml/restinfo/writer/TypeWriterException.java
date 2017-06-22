package at.doml.restinfo.writer;

import java.io.IOException;

public class TypeWriterException extends RuntimeException {

    public TypeWriterException(IOException cause) {
        super(cause);
    }

    @Override
    public synchronized IOException getCause() {
        return (IOException) super.getCause();
    }
}
