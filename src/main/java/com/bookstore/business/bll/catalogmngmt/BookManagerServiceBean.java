
package com.bookstore.business.bll.catalogmngmt;

import com.bookstore.business.persistence.catalog.Book;
import java.util.List;
import javax.annotation.PreDestroy;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.*;

/**
 * Service local de gestion des livres exposant une vue sans interface<br>
 * Composant basé sur le pattern Session facade.<br>
 * composant fine grained chargé des opérations CRUD relatives à la gestion des livres.<br>
 * accessible qu'en local - qu'au sein de la JVM où ce code s'exécute.<br>
 * Attribut transactionnel MANDATORY spécifié sur la classe : 
 * toutes les méthodes ne spécifiant pas explicitement un autre attribut transactionnel 
 * devront être invoquées dans un contexte transactionnel initié par un appelant.<br>
 */
@Stateless(name="BookManager")
@TransactionAttribute(TransactionAttributeType.MANDATORY)
@LocalBean //facultatif
public class BookManagerServiceBean {

    @PersistenceContext(unitName = "bsPU")
    EntityManager em;

   @PreDestroy
   void prevent(){
       System.out.println("instance va être détruite");
   }

   /**
    *
    * @param book livre à persister
    * sauvegarder en base l'état d'un livre nouvellement créé
    * @return le livre persisté
    */
    public Book saveBook(Book book){
        try{
            em.persist(book);
        }catch(IllegalArgumentException e){
            System.out.println("Got error while saving book");
        }
        System.out.println("book saved");
        return book;
    }

    /**
     * Retourner un livre en fonction d'une identité unique
     * @param bookId id livre recherché
     * @return le livre correspondant à l'id passée en argument
     */
    public Book findBookById(Long bookId) {
        return em.find(Book.class, bookId);
    }

 /**
 *
 * supprimer un livre
 * @param book le livre a supprimé. Si book est null, l'opération de suppression n'est pas exécutée
 */
    public void deleteBook(Book book) {
        try{
            em.remove(book);
        }catch(IllegalArgumentException e){
            System.out.println("Got error while removing book");
        }
        System.out.println("book removed");
    }

    /**
     * retourner une liste de livres dont le titre contient l'argument passé en paramètre.
     * Comportement transactionnel redéfini (SUPPORTS)
     * @param pattern motif permettant de retrouver une liste de livres
     * contenant le motif dans le titre
     * @return la liste des livres possédant le motif dans le titre
     */ 
    @TransactionAttribute(TransactionAttributeType.SUPPORTS) //méthode pouvant joindre le contexte transactionnel de l'appelant
    public List<Book> findByCriteria(String pattern) {
        String jpql = "select l from livres l where l.title like :custReq";
        pattern = "%"+pattern+"%";
        List<Book> result = null;
        System.out.println("Start Searching by pattern");
        try{
            TypedQuery<Book> query = em.createQuery(jpql, Book.class);
            query.setParameter("custReq", pattern);
            result = query.getResultList();
            System.out.println(result);
        }catch(IllegalArgumentException e){
            System.out.println("requête est mal formée " + e);
        }
        return result;
    }

    /**
     * modifier un livre et synchroniser l'état modifié avec la base.
     * Le comportement transactionnel de la méthode est redéfini. l'attribut transactionnel est REQUIRED.
     * Ainsi l'opération de mise à jour nécessitant un contexte transactionnel peut être invoquée depuis un bean CDI ne s'exécutant pas dans une transaction.
     * @param book livre existant (dans la base) à modifier
     * @return le livre modifié
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Book updateBook(Book book) {
        System.out.println("updating book");
        try{
            em.merge(book);
        }catch(IllegalArgumentException e){
            System.out.println("Got error while updating book");
        }
        System.out.println("books updated");
        return book;
    }

}
