package com.abi.dto;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.abi.util.ClaimBookConstants;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class CustomDateTimeDeserializer extends JsonDeserializer<Date> {
    @Override
    public Date deserialize(JsonParser jsonparser, DeserializationContext arg1)
	    throws IOException, JsonProcessingException {
	SimpleDateFormat format = new SimpleDateFormat(ClaimBookConstants.DEFAULT_JSON_DATE_FORMAT);
	String date = jsonparser.getText();
	try {
	    return format.parse(date);
	} catch (ParseException e) {
	    throw new RuntimeException(e);
	}

    }
}
