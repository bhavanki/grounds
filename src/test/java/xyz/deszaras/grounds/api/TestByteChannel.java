package xyz.deszaras.grounds.api;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.charset.StandardCharsets;

/**
 * A byte channel for testing purposes.
 */
public class TestByteChannel implements ByteChannel {

  private final ByteBuffer content;
  private final ByteBuffer output;
  private boolean closed;

  /**
   * Creates a new byte channel with the given content for reading. The content
   * is converted to UTF-8 bytes. The output buffer is initialized to 1024
   * bytes.
   *
   * @param  content content
   */
  public TestByteChannel(String content) {
    this(ByteBuffer.wrap(content.getBytes(StandardCharsets.UTF_8)), 1024);
  }

  /**
   * Creates a new byte channel with the given content for reading. The output
   * buffer is initialized to 1024 bytes.
   *
   * @param  content content
   */
  public TestByteChannel(ByteBuffer content) {
    this(content, 1024);
  }

  /**
   * Creates a new byte channel with the given content for reading.
   *
   * @param  content        content
   * @param  outputCapacity size of output buffer
   */
  public TestByteChannel(ByteBuffer content, int outputCapacity) {
    this.content = content;
    output = ByteBuffer.allocate(outputCapacity);
    closed = false;
  }

  @Override
  public int read(ByteBuffer dst) throws ClosedChannelException {
    if (closed) {
      throw new ClosedChannelException();
    }
    if (!content.hasRemaining()) {
      return -1;
    }
    int ct = Math.min(dst.remaining(), content.remaining());
    for (int i = 0; i < ct; i++) {
      dst.put(content.get());
    }
    return ct;
  }

  @Override
  public int write(ByteBuffer src) throws ClosedChannelException {
    if (closed) {
      throw new ClosedChannelException();
    }
    int ct = src.remaining();
    output.put(src);
    return ct;
  }

  @Override
  public boolean isOpen() {
    return !closed;
  }

  @Override
  public void close() {
    closed = true;
  }

  /**
   * Gets the output written to this channel.
   *
   * @return output
   */
  public byte[] getOutput() {
    return output.array();
  }

  /**
   * Gets the output written to this channel as a UTF-8 string.
   *
   * @return output as a string
   */
  public String getStringOutput() {
    return new String(getOutput(), StandardCharsets.UTF_8);
  }
}

