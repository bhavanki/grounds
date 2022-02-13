package xyz.deszaras.grounds.api.method;

import xyz.deszaras.grounds.api.JsonRpcRequest;
import xyz.deszaras.grounds.api.JsonRpcResponse;

/**
 * An interface for classes that implement API methods. An implementation of
 * this interface must be thread-safe, as an individual instance may server
 * several unrelated calls.
 */
public interface ApiMethod {

  /**
   * Calls this method.
   *
   * @param  request API request
   * @param  ctx     context for call
   * @return         API response
   */
  JsonRpcResponse call(JsonRpcRequest request, ApiMethodContext ctx);

}
