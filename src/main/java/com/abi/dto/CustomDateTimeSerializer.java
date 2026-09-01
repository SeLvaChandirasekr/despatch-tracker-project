package com.abi.dto;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.abi.util.ClaimBookConstants;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class CustomDateTimeSerializer extends JsonSerializer<Date> {

    @Override
    public void serialize(Date dateObject, JsonGenerator generator, SerializerProvider provider)
	    throws IOException, JsonProcessingException {
	SimpleDateFormat formatter = new SimpleDateFormat(ClaimBookConstants.DEFAULT_JSON_DATE_FORMAT);
	String formattedDate = formatter.format((Date) dateObject);
	generator.writeString(formattedDate);
    }

}
