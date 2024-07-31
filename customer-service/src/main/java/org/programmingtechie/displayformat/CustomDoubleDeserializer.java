package org.programmingtechie.displayformat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

public class CustomDoubleDeserializer extends JsonDeserializer<Double> {
    private static final DecimalFormat df;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(',');
        df = new DecimalFormat("#,##0", symbols);
    }

    @Override
    public Double deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String value = p.getText();
        try {
            return df.parse(value).doubleValue();
        } catch (ParseException e) {
            throw new IOException("Failed to parse Double value: " + value, e);
        }
    }
}