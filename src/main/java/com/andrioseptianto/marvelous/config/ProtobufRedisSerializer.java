package com.andrioseptianto.marvelous.config;

import com.andrioseptianto.marvelous.MarvelCharactersResponse;
import com.google.protobuf.Message;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

public class ProtobufRedisSerializer implements RedisSerializer<Message> {

    @Override
    public byte[] serialize(Message message) throws SerializationException {
        return message != null ? message.toByteArray() : null;
    }

    @Override
    public Message deserialize(byte[] bytes) throws SerializationException {
        try {
            return MarvelCharactersResponse.parseFrom(bytes);
        } catch (Exception e) {
            return null;
        }
    }
}