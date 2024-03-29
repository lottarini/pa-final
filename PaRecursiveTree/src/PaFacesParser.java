/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.FileNotFoundException;
import java.util.LinkedList;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import sun.security.pkcs.ParsingException;

/**
 *
 * @author andrealottarini
 */
public class PaFacesParser {

    private PaFacesTokenizer scanner;
    private String lookahead;
    private boolean markup;

    public PaFacesParser(String filename) throws FileNotFoundException, XMLStreamException {
        scanner = new PaFacesTokenizer(filename);
        lookahead = scanner.next();
        markup = false;
        //manca la roba dove scrivere
    }

    public PaFacesObject parseComponent() throws XMLStreamException, ParsingException {
        System.out.println("Parse Component: " + lookahead);
        //Un po brutale?
        PaFacesObject machine;
        machine = parseElement();
        match("$");
        scanner.close();
        return machine;
    }

    private PaFacesObject parseElement() throws XMLStreamException, ParsingException {
        System.out.println("Parse Element: " + lookahead);
        String actual = lookahead;
        /* parso il primo TAG necessario e lo assegno al nodo che mi rappresenta*/
        PaFacesObject me = parseTag();
        if (scanner.sectionType != XMLStreamConstants.END_ELEMENT) {
            //C'E` UN TREE
            System.out.println("Parse Element: Elemento " + actual + " NON terminato analizzo il sottoalbero");
            //ESSENDO UN TREE LA SU ROBA STA NEI FIGLIOLI
            me.children = parseTree();
        }
        /* Either I parsed a tree or a single element I need to match its closing tag*/
        if (me != null) {
            LinkedList<PaFacesObject> sons = parseTree();
            if (sons != null) {
                me.children = sons;
            }
            parseClosingTag();
            return me;
        }
        return null;
    }

    private LinkedList<PaFacesObject> parseTree() throws XMLStreamException, ParsingException {
        System.out.println("Parse Tree: " + lookahead);

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

    // JUST CHECK WHETHER  A CLOSING TAG EXISTS AND TAKES THE NEW LOOKAHEAD SYMBOL
    // IT EXPLICETELY HAS NO RETURN STATUS
    private void parseClosingTag() throws XMLStreamException, ParsingException {
        System.out.println("Parse Closing Tag: " + lookahead);
        if (scanner.sectionType == XMLStreamConstants.END_ELEMENT) {
            System.out.println("Closing Tag Ho beccato l'end element e chiamo la next");
            lookahead = scanner.next();
        } else {
            throw new ParsingException();
        }
    }

    // SHOULD BE INVOKED ONLY ON A OPEN TAG and STOPS at the CLOSING TAG
    private PaFacesObject parseTag() throws XMLStreamException, ParsingException {
        System.out.println("Parse Tag: " + lookahead);

        if (scanner.sectionType == XMLStreamConstants.START_ELEMENT) {

            PaFacesElement ret;
            LinkedList<PaFacesAttributes> attr = new LinkedList<PaFacesAttributes>();
            String id = lookahead;

            System.out.println("parseTag: chiamo la next per vedere gli attributi");
            lookahead = scanner.next();
            attr = parseAttribute();
            System.out.println("La lista contiene " + attr.size());
            if (id.equals("using")) {
                System.out.println("PaFacesUSING: " + id);
                ret = new PaFacesUsing(id);
            } else if (id.equals("insert-head")) {
                System.out.println("PaFacesINSERT: " + id);
                ret = new PaFacesInsert(id);

            } else {
                if (markup) {
                    System.out.println("PaFacesISTANCE: " + id);
                    ret = new PaFacesIstance(id);
                } else {
                    System.out.println("PaFacesHTML: " + id);
                    ret = new PaFacesHtml(id);
                }

            }
            markup = false;
            ret.attr = attr;
            return ret;
        }
        return null;
    }

    private LinkedList<PaFacesAttributes> parseAttribute() throws XMLStreamException {
        System.out.println("Parse Attribute: " + lookahead);
        LinkedList<PaFacesAttributes> ret = new LinkedList<PaFacesAttributes>();
        String id = null, value = null;

        while (scanner.sectionType == XMLStreamConstants.ATTRIBUTE || scanner.sectionType == XMLStreamConstants.NAMESPACE) {
            //E` il nome
            if (scanner.retname) {
                id = lookahead;
            } //e` il valore
            else {
                value = lookahead;
                ret.add(new PaFacesAttributes(id, value));
                if (id.equals("code") && value.equals("generate")) {
                    System.out.println("BECCATO IL MARKUP");
                    markup = true;
                }
            }
            lookahead = scanner.next();
        }

        return ret;

    }

    private PaFacesObject parseText() throws XMLStreamException {
        System.out.println("Parse Text: " + lookahead);
        if (scanner.sectionType == XMLStreamConstants.CHARACTERS) {
            PaFacesText temp = new PaFacesText(lookahead);
            System.out.println("PaFacesTEXT");
            lookahead = scanner.next();
            return temp;

        }
        System.out.println("NOT TEXT");
        return null;

    }

    private void match(String s) throws ParsingException {
        if (!lookahead.equals(s)) {
            throw new ParsingException();
        }
    }
}
