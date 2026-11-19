package kr.co.deepple.common.support;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.ListOffsetsResult.ListOffsetsResultInfo;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.listener.MessageListenerContainer;

@TestComponent
public class KafkaConsumerLag implements DisposableBean {

  private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
  private final Admin admin;

  public KafkaConsumerLag(
      KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry, KafkaAdmin kafkaAdmin) {
    this.kafkaListenerEndpointRegistry = kafkaListenerEndpointRegistry;
    this.admin = Admin.create(kafkaAdmin.getConfigurationProperties());
  }

  public boolean isZero() throws ExecutionException, InterruptedException {
    Map<String, Set<String>> topicsByGroup = findTopicsByGroup();
    Set<String> topics =
        topicsByGroup.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
    if (topics.isEmpty()) {
      return true;
    }
    Map<TopicPartition, Long> endOffsets = findEndOffsets(topics);
    for (Map.Entry<String, Set<String>> entry : topicsByGroup.entrySet()) {
      if (hasLag(entry.getKey(), entry.getValue(), endOffsets)) {
        return false;
      }
    }
    return true;
  }

  private Map<String, Set<String>> findTopicsByGroup() {
    Map<String, Set<String>> topicsByGroup = new HashMap<>();
    for (MessageListenerContainer container :
        kafkaListenerEndpointRegistry.getListenerContainers()) {
      String[] topics = container.getContainerProperties().getTopics();
      if (topics == null) {
        continue;
      }
      topicsByGroup
          .computeIfAbsent(container.getGroupId(), groupId -> new HashSet<>())
          .addAll(Arrays.asList(topics));
    }
    return topicsByGroup;
  }

  private Map<TopicPartition, Long> findEndOffsets(Set<String> topics)
      throws ExecutionException, InterruptedException {
    Collection<TopicDescription> descriptions =
        admin.describeTopics(topics).allTopicNames().get().values();
    Map<TopicPartition, OffsetSpec> latest =
        descriptions.stream()
            .flatMap(description ->
                description.partitions().stream()
                    .map(
                        partition -> new TopicPartition(description.name(), partition.partition())))
            .collect(Collectors.toMap(Function.identity(), partition -> OffsetSpec.latest()));
    Map<TopicPartition, ListOffsetsResultInfo> offsets = admin.listOffsets(latest).all().get();
    return offsets.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().offset()));
  }

  private boolean hasLag(String groupId, Set<String> topics, Map<TopicPartition, Long> endOffsets)
      throws ExecutionException, InterruptedException {
    Map<TopicPartition, OffsetAndMetadata> committed =
        admin.listConsumerGroupOffsets(groupId).partitionsToOffsetAndMetadata().get();
    return endOffsets.entrySet().stream()
        .filter(entry -> topics.contains(entry.getKey().topic()))
        .anyMatch(entry -> committedOffset(committed.get(entry.getKey())) < entry.getValue());
  }

  private static long committedOffset(OffsetAndMetadata offsetAndMetadata) {
    return offsetAndMetadata == null ? 0L : offsetAndMetadata.offset();
  }

  @Override
  public void destroy() {
    admin.close();
  }
}
