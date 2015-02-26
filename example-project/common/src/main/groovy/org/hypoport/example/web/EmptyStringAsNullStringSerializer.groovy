package org.hypoport.example.web

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper
import com.fasterxml.jackson.databind.ser.std.NonTypedScalarSerializerBase
import org.apache.commons.lang.StringUtils

import java.lang.reflect.Type

class EmptyStringAsNullStringSerializer extends NonTypedScalarSerializerBase<String> {

  public EmptyStringAsNullStringSerializer() {
    super(String)
  }

  @Override
  public boolean isEmpty(String value) {
    return (value == null) || (value.length() == 0)
  }

  @Override
  public void serialize(String value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
    if (StringUtils.isBlank(value) &&
        provider.config.getSerializationInclusion().equals(JsonInclude.Include.NON_EMPTY)) {
      jgen.writeNull()
    }
    else {
      jgen.writeString(value)
    }
  }

  @Override
  public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
    return createSchemaNode("string", true)
  }

  @Override
  public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
    if (visitor != null) {
      visitor.expectStringFormat(typeHint)
    }
  }
}
