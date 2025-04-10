package com.geezylucas.democloudfunctionhttpspringboot.dto;

import java.util.Map;

/**
 * A class that can be mapped to the GCF Pub/Sub Message event type. This is for use in
 * the background functions.
 *
 * <p>See the PubSubMessage definition for reference:
 * <a href="https://cloud.google.com/pubsub/docs/reference/rest/v1/PubsubMessage">PubsubMessage V1</a>
 *
 * @author Mike Eltsufin
 */
public record PubSubMessageDTO(String data,
                               Map<String, String> attributes,
                               String messageId,
                               String publishTime) {
}
