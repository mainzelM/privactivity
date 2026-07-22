package net.privactivity.tcx;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXParseException;

class TcxXmlUtilTest {

    @Test
    void testRejectsXmlWithDoctype() {
        TcxXmlUtil testee = new TcxXmlUtil();
        String maliciousXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE TrainingCenterDatabase [
                  <!ENTITY xxe "boom">
                ]>
                <TrainingCenterDatabase xmlns="http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2">
                  <Author>&xxe;</Author>
                </TrainingCenterDatabase>
                """;

        assertThatThrownBy(() -> testee.unmarshall(
                new ByteArrayInputStream(maliciousXml.getBytes(StandardCharsets.UTF_8))))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unable to unmarshal file")
                .hasRootCauseInstanceOf(SAXParseException.class);
    }
}
