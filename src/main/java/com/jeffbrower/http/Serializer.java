package com.jeffbrower.http;

public interface Serializer {
  byte[] serialize(Object object);
}
