package Features;

import PaFaces.*;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import sun.security.pkcs.ParsingException;

public class PaFacesParser {

    private PaFacesTokenizer scanner;
    private String lookahead;
    private boolean markup;

    public PaFacesParser(String filename) throws FileNotFoundException, XMLStreamException {
        scanner = new PaFacesTokenizer(filename);
        lookahead = scanner.next();
        markup = false;
    }

    public PaFacesObject parseComponent() throws XMLStreamException, ParsingException {
        
        PaFacesObject machine;
        machine = parseElement();
        match("$");
        scanner.close();
        return machine;
    }

    private PaFacesObject parseElement() throws XMLStreamException, ParsingException {
        
        /* parso il primo TAG necessario e lo assegno al nodo che mi rappresenta*/
        PaFacesObject machine = parseTag();
        if (scanner.sectionType != XMLStreamConstants.END_ELEMENT) {
            //C'E` UN TREE
            
            //ESSENDO UN TREE LA SU ROBA STA NEI FIGLIOLI
            machine.children = parseTree();
        }
        /* Either I parsed a tree or a single element I need to match its closing tag*/
        if (machine != null) {
            LinkedList<PaFacesObject> sons = parseTree();
            if (sons != null) {
                machine.children = sons;
            }
            parseClosingTag();
            return machine;
        }
        return null;
    }

    private LinkedList<PaFacesObject> parseTree() throws XMLStreamException, ParsingException {
        

        PaFacesObject actual = parseText();
        if (actual == null) {
            actual = parseElement();
        }

        if (actual != null) {
            LinkedList<PaFacesObject> ret = new LinkedList<PaFacesObject>();
            ret.add(actual);
            LinkedList<PaFacesObject> next;
            next = parseTree();
            if (next != null) {
                ret.addAll(next);
            }
            return ret;
        } else {
            return null;
        }
    }

    // THIS METHOD EXPLICETELY HAS NO RETURN STATUS
    private void parseClosingTag() throws XMLStreamException, ParsingException {
        
        if (scanner.sectionType == XMLStreamConstants.END_ELEMENT) {
            
            lookahead = scanner.next();
        } else {
            throw new ParsingException();
        }
    }

    private PaFacesObject parseTag() throws XMLStreamException, ParsingException {
        if (scanner.sectionType == XMLStreamConstants.START_ELEMENT) {

            PaFacesElement ret;
            LinkedList<PaFacesAttribute> attributes = new LinkedList<PaFacesAttribute>();
            String name = "";
            String id = lookahead;
            lookahead = scanner.next();
            PaFacesAttribute attr = parseAttribute();

            while (attr != null) {

                attributes.add(attr);
                if (attr.id.equals("code") && attr.value.equals("generate")) {
                    markup = true;
                }
                if (attr.id.equals("id")) {
                    name = attr.value;
                }
                attr = parseAttribute();
            }

            if (id.equals("using")) {
                ret = new PaFacesUsing(id);
            } else if (id.equals("insert-head")) {
                ret = new PaFacesInsert(id);
            } else {
                if (markup) {
                    ret = new PaFacesInstance(id, name);
                } else {
                    ret = new PaFacesHtml(id);
                }
            }
            markup = false;
            ret.attr = attributes;
            return ret;
        }
        return null;
    }

    private PaFacesAttribute parseAttribute() throws XMLStreamException {


        String id = null, value = null;

        if (scanner.sectionType == XMLStreamConstants.ATTRIBUTE || scanner.sectionType == XMLStreamConstants.NAMESPACE) {
            id = lookahead;
            lookahead = scanner.next();
            value = lookahead;
            lookahead = scanner.next();
            return new PaFacesAttribute(id, value);
        }

        return null;

    }

    private PaFacesObject parseText() throws XMLStreamException {
        
        if (scanner.sectionType == XMLStreamConstants.CHARACTERS) {
            PaFacesText temp = new PaFacesText(lookahead);
           
            lookahead = scanner.next();
            return temp;

        }
        
        return null;

    }

    private void match(String s) throws ParsingException {
        if (!lookahead.equals(s)) {
            throw new ParsingException();
        }
    }
}