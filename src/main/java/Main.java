import org.jdom2.*;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
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

        XPathFactory xpfac = XPathFactory.instance();

        // -- Recherche dans docSource les éléments "Prpjection"
        XPathExpression xp = xpfac.compile("//Projection", Filters.element());
        List<Element> resultat = (List<Element>) xp.evaluate(docSource);

        // -- Ajout de l'élément "Projections" à l'élément "plex"
        Element elemProjections = new Element("projections");
        for (Element projectionSource : resultat) {
            Element elemProjection = new Element("projection");
            elemProjection.setAttribute("film_id", "F" + projectionSource.getChild("film").getAttributeValue("id"));
            elemProjection.setAttribute("titre", projectionSource.getChild("film").getChild("titre").getText());

            // -- Récupération de l'élément salle de la source et création d'un nouvel élément salle selon la dtd
            Element salleSource = projectionSource.getChild("salle");
            Element elemSalle = new Element("salle").setText(salleSource.getChildText("no"));
            elemSalle.setAttribute("taille", salleSource.getChildText("taille"));

            // -- Récupération de l'élément dateHeure de la source et création d'un nouvel élément dateHeure selon la dtd
            Element dateHeureSource = projectionSource.getChild("dateHeure");
            Element elemDateHeure = new Element("dateHeure").setText(dateHeureSource.getText());
            elemDateHeure.setAttribute("format", dateHeureSource.getAttributeValue("format"));

            elemProjection.addContent(elemSalle);
            elemProjection.addContent(elemDateHeure);
            elemProjections.addContent(elemProjection);
        }

        // -- Ajout de l'élément films à ll'élément racine
        Element elemFilms = new Element("films");

        // -- Recherche dans docSource les éléments "films"
        xp = xpfac.compile("//film", Filters.element());
        resultat = xp.evaluate(docSource);
        for (Element filmSource : resultat) {
            Element elemFilm = new Element("film");
            // -- Ajout des éléments spécifiés dans la dtd en allant les chercher dans le film source
            Element elemTitre = new Element("titre").setText(filmSource.getChildText("titre"));
            Element elemDuree = new Element("duree").setText(filmSource.getChildText("duree"));
            Element elemSynopsis = new Element("synopsis").setText(filmSource.getChildText("synopsis"));

            // -- Ajout de l'élément photo uniquement si il existe dans le xml original
            if (filmSource.getChild("photo") != null) {
                Element photo = new Element("photo");
                photo.setAttribute("url", filmSource.getChildText("photo"));
                elemFilm.addContent(photo);
            }

            // -- Ajout de l'élément critiques
            Element elemCritiques = new Element("critiques");
            if (filmSource.getChild("critiques").getChildren() != null) {
                for (Element critiqueSource : filmSource.getChild("critiques").getChildren()) {
                    Element elemCritique = new Element("critique").setText(critiqueSource.getText());
                    if (critiqueSource.getAttribute("note") != null) {
                        // -- Ajout de l'attribut note à l'élément critique si il existe
                        elemCritique.setAttribute("note", critiqueSource.getAttributeValue("note"));
                    }
                    elemCritiques.addContent(elemCritique);
                }
            }

            // -- Ajout de l'élément langages
            Element elemLangages = new Element("langages");

            // -- Ajout de l'élément genres
            Element elemGenres = new Element("genres");

            // -- Ajout de l'élément mots_cles
            Element elemMotscles = new Element("mots_cles");

            // -- Ajout de l'élément roles
            Element elemRoles = new Element("roles");

            // -- Ajout de tous les roles d'un film donné comme éléments vides (comme spécifié dans la dtd)
            for (Element roleSource : filmSource.getChild("Roles").getChildren()) {
                Element elemRole = new Element("role");
                if (roleSource.getChild("place") != null)
                    elemRole.setAttribute("place", roleSource.getChildText("place"));
                else elemRole.setAttribute("place", "n/a");
                if (roleSource.getChild("personnage") != null)
                    elemRole.setAttribute("personnage", roleSource.getChildText("personnage"));
                else elemRole.setAttribute("personnage", "n/a");
                elemRole.setAttribute("acteur_id", "A" + roleSource.getChild("Acteur").getAttributeValue("id"));
                elemRoles.addContent(elemRole);
            }

            // -- Ajout de l'attribut no à l'élément film
            elemFilm.setAttribute("no", "F" + filmSource.getAttributeValue("id"));

            // -- Ajout de l'attribut format à l'élément duree
            elemDuree.setAttribute("format", "minutes");


            setListOfAttributes(elemLangages, filmSource, "langages", "liste", "L");
            setListOfAttributes(elemGenres, filmSource, "genres", "liste", "G");
            setListOfAttributes(elemMotscles, filmSource, "motcles", "liste", "M");

            //TODO ici continuer



            // -- Ajout de tous les éléments précédemment définis dans l'élément film
            elemFilm.addContent(elemTitre)
                    .addContent(elemDuree)
                    .addContent(elemSynopsis)
                    .addContent(elemCritiques)
                    .addContent(elemLangages)
                    .addContent(elemGenres)
                    .addContent(elemMotscles)
                    .addContent(elemRoles);

            elemFilms.addContent(elemFilm);
        }

        // -- Ajout de l'élément acteurs à l'élément racine
        Element elemActeurs = new Element("acteurs");

        // -- Recherche dans docSource les éléments "Acteur"
        xp = xpfac.compile("//Acteur", Filters.element());
        resultat = xp.evaluate(docSource);
        for (Element acteurSource : resultat) {
            // -- Construction des éléments spécifiées dans la dtd pour un acteur donné
            Element elemActeur = new Element("acteur");
            Element elemNom = new Element("nom").setText(acteurSource.getChildText("nom"));
            Element elemNomNaissance = new Element("nom_naissance").setText(acteurSource.getChildText("nomNaissance"));
            Element elemSexe = new Element("sexe");
            Element elemDateNaissance = new Element("date_naissance").setText(acteurSource.getChildText("dateNaissance"))
                    .setAttribute("format", acteurSource.getChild("dateNaissance").getAttributeValue("format"));

            // -- Définition de l'attribut valeur dans l'élément sexe uniquemenent si exixatant dans le xml d'origine car #IMPLIED
            if (acteurSource.getAttribute("sexe") != null)
                elemSexe.setAttribute("sexe", acteurSource.getAttributeValue("sexe"));

            // -- Définition des attributs d'un acteur
            elemActeur.setAttribute("no", acteurSource.getAttributeValue("id"));

            // -- Rajout des éléments déinis précédemment dans l'élément acteur
            elemActeur.addContent(elemNom)
                    .addContent(elemNomNaissance)
                    .addContent(elemSexe)
                    .addContent(elemDateNaissance);

            // -- Rajout de l'élément date_deces uniquement si il existe dans le xml d'origine
            if (acteurSource.getChild("dateDeces") != null) {
                Element elemDateDeces = new Element("date_deces")
                        .setAttribute("format", acteurSource.getChild("dateDeces").getAttributeValue("format"));
                elemActeur.addContent(elemDateDeces);
            }

            // -- Rajout de l'élément biographie uniquement si il existe dans le xml d'origine
            if (acteurSource.getChild("biographie") != null) {
                Element elemBiographie = new Element("biographie").setText(acteurSource.getChildText("biographie"));
                elemActeur.addContent(elemBiographie);
            }

                elemActeurs.addContent(elemActeur);
        }

        // -- Ajout de l'élément liste_langages à l'élément racine
        Element elemLangages = new Element("liste_langages");

        // -- Recherche dans docSource les éléments "langage"
        xp = xpfac.compile("//langage", Filters.element());
        resultat = xp.evaluate(docSource);

        // -- Utilisation d'un HashSet pour l'élimination des doublons
        HashSet<Element> langages = new HashSet<Element>();
        for (Element langageSource : resultat) {
            Element elemLangage = new Element("langage").setText(langageSource.getText())
                    .setAttribute("no", langageSource.getAttributeValue("id"));
            addElem(elemLangage, langages);
        }
        for (Element elemLangage : langages) {
            elemLangages.addContent(elemLangage);
        }

        // -- Ajout de l'élément liste_genres à l'élément racine
        Element elemGenres = new Element("liste_genres");

        // -- Recherche dans docSource les éléments "genre"
        xp = xpfac.compile("//genre", Filters.element());
        resultat = xp.evaluate(docSource);

        // -- Utilisation d'un HashSet pour l'élimination des doublons
        HashSet<Element> genres = new HashSet<Element>();
        for (Element genreSource : resultat) {
            Element elemGenre = new Element("genre").setText(genreSource.getText());
            elemGenre.setAttribute("no", genreSource.getAttributeValue("id"));
            addElem(elemGenre, genres);
        }
        for (Element elemGenre : genres) {
            elemGenres.addContent(elemGenre);
        }

        // -- Ajout de l'élément liste_mots_cles à l'élément racine
        Element elemMotcles = new Element("liste_mots_cles");

        // -- Recherche dans docSource les éléments "motcle"
        xp = xpfac.compile("//motcle", Filters.element());
        resultat = xp.evaluate(docSource);

        // -- Utilisation d'un HashSet pour l'élimination des doublons
        HashSet<Element> motCles = new HashSet<Element>();
        for (Element motcleSource : resultat) {
            Element elemMotcle = new Element("mot_cle").setText(motcleSource.getChildText("label"))
                    .setAttribute("no", motcleSource.getAttributeValue("id"));
            addElem(elemMotcle, motCles);
        }
        for (Element elemMotcle : motCles) {
            elemMotcles.addContent(elemMotcle);
        }

        // - Ajout à l'élément racine les éléments selon la dtd
        plex.addContent(elemProjections)
                .addContent(elemFilms)
                .addContent(elemActeurs)
                .addContent(elemLangages)
                .addContent(elemGenres)
                .addContent(elemMotcles);

        docProjections.addContent(plex);

        // -- Enregistrer le résultat dans le fichier projections.xml
        enregistrer(docProjections, "projections.xml");
    }

    /**
     * Rajoute un élément dans un ensemble uniquement s'il n'y a pas de doublons.
     * Un doublon est considéré comme tel dans cet exemple si il a le même attribut "no" ou "id" qu'un autre élément dans
     * l'ensemble
     *
     * @param element element à ajouter
     * @param set ensemble d'éléments
     *
     */
    private static void addElem(Element element, HashSet<Element> set){
        for (Element elem : set){
            if (elem.getAttribute("no") != null) {
                if (elem.getAttributeValue("no").equals(element.getAttributeValue("no"))) {
                    return;
                }
            }
            else if (elem.getAttribute("id") != null){
                if (elem.getAttributeValue("id").equals(element.getAttributeValue("id"))) {
                    return;
                }
            }
        }
        set.add(element);
    }

    /**
     *
     * RAjoute une liste d'attrributs à un élément à parir de son élément source situé dans un autre fichier xml.
     * Typiquement utilisé dans ce cas pour les attributs marqués #IDREFS qui référencent d'autres éléments dans le
     * fichier
     * Le but est de ne pas introduire des douublons
     *
     *
     * @param elem élément auquel on rajoute l'attribut
     * @param sourceElem élément source depuis lequel on
     * @param commonChildrenName le nom commun des enfants de l'élément source
     * @param attName nom de l'attribut à rajouter
     * @param prefix nom de préfixe rajouté à la valeur de l'attribut
     */
    private static void setListOfAttributes(Element elem, Element sourceElem, String commonChildrenName, String attName, String prefix){
        StringBuilder sb = new StringBuilder();
        HashSet<Element> elementContainer = new HashSet<Element>();
        for (Element elemLangage : sourceElem.getChild(commonChildrenName).getChildren()){
            addElem(elemLangage, elementContainer);
        }
        for (Element elemLangage : elementContainer){
            sb.append(prefix + elemLangage.getAttributeValue("id")).append(" ");
        }
        elem.setAttribute(attName, sb.toString());
    }
}