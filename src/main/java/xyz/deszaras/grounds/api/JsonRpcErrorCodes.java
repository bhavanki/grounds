package xyz.deszaras.grounds.api;

/**
 * Standard JSON RPC error codes.
 */
public final class JsonRpcErrorCodes {

  public static final int PARSE_ERROR = -32700;
  public static final int INVALID_REQUEST = -32600;
  public static final int METHOD_NOT_FOUND = -32601;
  public static final int INVALID_PARAMETERS = -32602;
  public static final int INTERNAL_ERROR = -32603;

  private JsonRpcErrorCodes() {
  }
}
