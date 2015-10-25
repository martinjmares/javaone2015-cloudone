package cloudone.client;

import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Entity;

import cloudone.ServiceFullName;

/**
 * Invokes REST call any time later using independent subsystem.
 * <p>
 * Call invocation done by this API enqueue task on underlying subsytem which act independently
 * on the environment. It is garanteed that if call is enquede then it will be ones processed.
 * <p>
 * Response of the call is information if call was successfully enqueued. HTTP success code refers
 * to the successfully enqued item. Payload is string identification of enqued item which
 * can be used for monitoring and identification of resource response.
 * <p>
 * Responses from reached resources are not processed in traditional way but it can be forwarded
 * to defined resource using responseTo method.
 * <p>
 * Guaranteed qualities:
 * <ul>
 *     <li><b>order:</b> Each {@code path} represents one FIFO queue.</li>
 *     <li><b>retention:</b> Retention policy of each item is defined on the item.</li>
 * </ul>
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public interface LaterInvoker {

    /** Defined that response from final resource(s) must be forwarded to this application
     *  on the resource defined by provided path parameter.
     *  <p>
     *  Method {@code POST} will be used for the call.
     *  <p>
     *  Payload will be in the form of original response payload including content type and headers.
     *  <p>
     *  In case of cluster, it is not garanteed that response will reach only this instance.
     *  <p>
     *  Other information will be provided using query parameters:
     *  <ul>
     *      <li><b>id</b>: Id of the original request.</li>
     *      <li><b>response-code:</b>: HTTP response code.</li>
     *  </ul>
     * @param path of the resource where response should be forwarded.
     * @return LaterInvoker for future invocation.
     */
    public LaterInvoker responseTo(String path);

    /** Defined that response from final resource(s) must be forwarded to this application
     *  on the resource defined by provided path parameter.
     *  <p>
     *  Method {@code POST} will be used for the call.
     *  <p>
     *  Payload will be in the form of original response payload including content type and headers.
     *  <p>
     *  Other information will be provided using query parameters:
     *  <ul>
     *      <li><b>id</b>: Id of the original request.</li>
     *      <li><b>response-code:</b>: HTTP response code.</li>
     *  </ul>
     *
     * @param service which will be called for the result.
     * @param path of the resource where response should be forwarded.
     * @return LaterInvoker for future invocation.
     */
    public LaterInvoker responseTo(ServiceFullName service, String path);

    /**
     * Invocation will remain on the system until {@code count} new items will be provided
     * to the same resource path.
     * <p>
     * If it is combined with other retention options then first applicable option will be used
     * to retain invocation from the queue.
     *
     * @param count of newer items in queue which will cause remove of this invocation.
     * @return LaterInvoker for future invocation.
     */
    public LaterInvoker retentionCount(int count);

    /**
     * Invocation will be removed from the system after the defined timeout.
     * <p>
     * If it is combined with other retention options then first applicable option will be used
     * to retain invocation from the queue.
     *
     * @param period time amaunt until retention.
     * @param timeUnit for {@code period} parameter.
     * @return LaterInvoker for future invocation.
     */
    public LaterInvoker retentionTime(long period, TimeUnit timeUnit);

    /**
     * Invokes this invocation using provided http method and entity.
     *
     * @param name of http method.
     * @param entity which will be included.
     * @return id of the invocation task.
     */
    String method(String name, Entity<?> entity);

    String method(String name);

    String get();

    String put(Entity<?> entity);

    String post(Entity<?> entity);

    String delete();

    String head();

    String options();

}
