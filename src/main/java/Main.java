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

        // Ajout d'une référence à la DTD
        DocType docType = new DocType("plex", "projections.dtd");
        docProjections.addContent(docType);

        // Ajout de la référence à la feuille XSL
        ProcessingInstruction piXSL = new ProcessingInstruction("xml-stylesheet");
        HashMap<String, String> piAttributes = new HashMap<String, String>();
        piAttributes.put("type", "text/xsl");
        piAttributes.put("href", "projections.xsl");
        piXSL.setData(piAttributes);
        docProjections.addContent(piXSL);

        // Ajout de élément racine "plex"
        Element plex = new Element("plex");

        // Filtre xpath permettant de rechercher dans le document XML
        XPathFactory xPathFactory = XPathFactory.instance();


        // Noms des noeuds dans le document originel
        String nodeProjections = "Projections";
        String nodeProjection = "Projection";

        //nom noeud dans le docuement final
        String nodeProjectionsFinal = "projections";
        String nodeProjectionFinal = "projection";

        //Attributs document originel
        String attrID = "id";

        //Attribut document final
        String attrDTDID = "film_id";
        String prefxAttrDTDID = "F";
        String attrDTDFilm = "film";
        String attrDTDTitre = "titre";



        // Créer un filtre et récupérer les noeuds dans projection
        Element Projections = new Element(nodeProjectionsFinal);
        XPathExpression<Element> expr = xPathFactory.compile("//"+nodeProjection, Filters.element());
        List<Element> listProj = expr.evaluate(docSource);

        System.out.println("Nombre de projection : " +listProj.size());
        for (Element e:
             listProj) {

            //System.out.println(e.getChildren().toString());

            //parametre les attributs des projections film_id et titre du film
            Element singleProj = new Element(nodeProjectionFinal);
            singleProj.setAttribute(attrDTDID,prefxAttrDTDID+e.getAttributeValue(attrID));
            singleProj.setAttribute(attrDTDTitre,e.getChild("film").getChildText("titre"));




            // ajouts des fils projection à projections
            Projections.addContent(singleProj);
        }



        plex.addContent(Projections);
        docProjections.addContent(plex);

        //Exemple d'utilisation XPath: Recherche des éléments "Genre" dans le fichier de départ

        /*XPathFactory xpfac = XPathFactory.instance();

        XPathExpression xp = xpfac.compile("//Projection", Filters.element());
        List<Element> resultat = (List<Element>) xp.evaluate(docSource);

        Element elemProjections = new Element("Projections");
        for (Element projectionSource : resultat) {
            String valeur = projectionSource.getText();
            Element elemProjection = new Element("Projection").setText(valeur);
            elemProjections.addContent(elemProjection);
        }
        plex.addContent(elemProjections);
        */
        // -- Enregistrer le résultat dans le fichier projections.xml
        enregistrer(docProjections, "projections.xml");
    }
}