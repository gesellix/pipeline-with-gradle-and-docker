package org.hypoport.example.web

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.joda.JodaModule

public class JsonObjectMapper extends ObjectMapper {

  public JsonObjectMapper() {
    setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.ANY)

    setSerializationInclusion(JsonInclude.Include.NON_EMPTY)

    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    disable(SerializationFeature.WRITE_NULL_MAP_VALUES)

    registerModule(new JodaModule())
    registerModule(new EmptyStringModule())
  }

  class EmptyStringModule extends SimpleModule {

    EmptyStringModule() {
      addSerializer(String, new EmptyStringAsNullStringSerializer())
    }
  }
}
