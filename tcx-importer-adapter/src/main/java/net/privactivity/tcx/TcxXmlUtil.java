package net.privactivity.tcx;

import com.garmin.xmlschemas.trainingcenterdatabase.v2.TrainingCenterDatabaseT;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.InputStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

class TcxXmlUtil {
    private final JAXBContext jaxbContext;
    private final Unmarshaller unmarshaller;

    TcxXmlUtil() {
        this.jaxbContext = createJaxbContext();
        this.unmarshaller = createUnmarshaller();
    }

    private JAXBContext createJaxbContext() {
        try {
            return JAXBContext.newInstance(TrainingCenterDatabaseT.class);
        } catch (JAXBException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public TrainingCenterDatabaseT unmarshall(InputStream file) {
        return unmarshallFile(file).getValue();
    }

    private JAXBElement<TrainingCenterDatabaseT> unmarshallFile(InputStream file) {
        try {
            return (JAXBElement<TrainingCenterDatabaseT>) unmarshaller.unmarshal(
                    new SAXSource(createSecureXmlReader(), new InputSource(file)));
        } catch (JAXBException e) {
            throw new RuntimeException("Unable to unmarshal file: ", e);
        }
    }

    private XMLReader createSecureXmlReader() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            return factory.newSAXParser().getXMLReader();
        } catch (ParserConfigurationException | SAXException e) {
            throw new IllegalStateException("Unable to create secure XML parser", e);
        } catch (UnsupportedOperationException e) {
            throw new IllegalStateException("Unable to disable insecure XML parser features", e);
        }
    }

    private Unmarshaller createUnmarshaller() {
        try {
            return jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

}
