package rs.kunpero.vacation.config.filter;

import com.google.common.primitives.Bytes;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class AuthenticationRequestWrapper extends HttpServletRequestWrapper {

    // tag::variables[]
    private byte[] requestBody = new byte[0];
    private boolean bufferFilled = false;
    // end::variables[]

    /**
     - Constructs a request object wrapping the given request.
     *
     - @param request The request to wrap
     - @throws IllegalArgumentException if the request is null
     */
    public AuthenticationRequestWrapper(HttpServletRequest request) {
        super(request);
    }


    // tag::getRequestBody[]
    public byte[] getRequestBody() throws IOException {
        if (bufferFilled) {
            return Arrays.copyOf(requestBody, requestBody.length);
        }

        InputStream inputStream = super.getInputStream();

        byte[] buffer = new byte[102400]; // 100kb buffer

        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            requestBody = Bytes.concat(this.requestBody, Arrays.copyOfRange(buffer, 0, bytesRead)); // <1>
        }

        bufferFilled = true;

        return requestBody;
    }
    // end::getRequestBody[]

    // tag::getInputStream[]
    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new CustomServletInputStream(getRequestBody()); // <1>
    }
    // end::getInputStream[]

    private static class CustomServletInputStream extends ServletInputStream {

        private ByteArrayInputStream buffer;

        public CustomServletInputStream(byte[] contents) {
            this.buffer = new ByteArrayInputStream(contents);
        }

        @Override
        public int read() throws IOException {
            return buffer.read();
        }

        @Override
        public boolean isFinished() {
            return buffer.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener listener) {
            throw new RuntimeException("Not implemented");
        }
    }
}
