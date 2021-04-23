import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;
import java.io.*;

public class FixDb
{

    public static void main(String[] args) throws Exception
    {
        File source = new File("..\\resources\\object-views.xsd");
        if(!source.exists())
        {
            System.out.println("File:"+source+ " does not exist");
        }

        File target = new File(source+".new");

        XMLInputFactory  inputFactory = XMLInputFactory.newInstance();
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        InputStream in = new FileInputStream(source);
        XMLEventReader reader = inputFactory.createXMLEventReader(in);

        OutputStream out = new FileOutputStream(target);
        XMLEventWriter writer =  outputFactory.createXMLEventWriter(out);
        XMLEvent event;

        boolean deleteSection = false;
        while(reader.hasNext())
        {
            event = reader.nextEvent();
            if(event.getEventType() == XMLStreamConstants.START_ELEMENT){
                System.out.println(event.asStartElement().getName().toString());
            }
            if(event.getEventType() == XMLStreamConstants.START_ELEMENT && event.asStartElement().getName().toString().equalsIgnoreCase("{http://www.w3.org/2001/XMLSchema}annotation"))
            {
                deleteSection=true;
                continue;
            }
            else if(event.getEventType() == XMLStreamConstants.END_ELEMENT && (event.asEndElement().getName().toString().equalsIgnoreCase("{http://www.w3.org/2001/XMLSchema}annotation")))
            {
                deleteSection=false;
                continue;
            }
            else if(deleteSection)
            {
                continue;
            }
            else
            {
                writer.add(event);
            }
        }
    }
}
