import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.ProcessingInstruction;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;

public class Main {


    private static void enregistrer(Document document, String nomFichier) {
        try {
            //Utilisation d'un affichage classique avec getPrettyFormat()
            XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
            //Pour effectuer la sérialisation, il suffit simplement de créer
            // une instance de FileOutputStream en passant le nom du fichier
            sortie.output(document, new FileOutputStream(nomFichier));
        } catch (java.io.IOException e) {
        }
    }


    public static void main(String argv[]) throws Exception {

        // Lecture du document source XML
        SAXBuilder builder = new SAXBuilder();
        Document docSource = builder.build(new File("SER_Lab2.xml"));

        // Construction du document résultat
        Document docProjections = new Document();

        // -- Ajout d'une référence à la DTD
        DocType docType = new DocType("plex", "projections.dtd");
        docProjections.addContent(docType);

        // -- Ajout de la référence à la feuille XSL
        ProcessingInstruction piXSL = new ProcessingInstruction("xml-stylesheet");
        HashMap<String, String> piAttributes = new HashMap<String, String>();
        piAttributes.put("type", "text/xsl");
        piAttributes.put("href", "projections.xsl");
        piXSL.setData(piAttributes);
        docProjections.addContent(piXSL);

        // -- Ajout élément racine "plex"
        Element plex = new Element("plex");




        //Exemple d'utilisation XPath: Recherche des éléments "Genre" dans le fichier de départ

        XPathFactory xpfac = XPathFactory.instance();

        XPathExpression xp = xpfac.compile("//Genre", Filters.element());
        List<Element> resultat = xp.evaluate(docSource);

        Element elemGenres = new Element("Genres");
        for (Element genreSource : resultat) {
            String valeur = genreSource.getText();
            Element elemGenre = new Element("Genre").setText(valeur);
            elemGenres.addContent(elemGenre);
        }
        plex.addContent(elemGenres);
        // -- Enregistrer le résultat dans le fichier projections.xml
        enregistrer(docProjections, "projections.xml");
    }
}