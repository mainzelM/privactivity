package net.privactivity.tcx.jaxb;


import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.time.Instant;

public class DateAdapter extends XmlAdapter<String, Instant> {

    @Override
    public Instant unmarshal(String dateTime) throws Exception {
        return Instant.parse(dateTime);
    }

    @Override
    public String marshal(Instant instant) throws Exception {
        return instant.toString();
    }
}
