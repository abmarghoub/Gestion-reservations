package com.example;

import com.example.model.*;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class ConsoleApp {
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("gestion-reservations");
    private static final EntityManager em = emf.createEntityManager();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        initialiserDonnees();

        while (true) {
            System.out.println("\n=== Menu Gestion des Réservations ===");
            System.out.println("1. Lister les salles");
            System.out.println("2. Vérifier la disponibilité d’une salle");
            System.out.println("3. Créer une réservation");
            System.out.println("0. Quitter");
            System.out.print("Votre choix : ");

            int choix = Integer.parseInt(scanner.nextLine());

            switch (choix) {
                case 1 -> listerSalles();
                case 2 -> verifierDisponibilite();
                case 3 -> creerReservation();
                case 0 -> {
                    em.close();
                    emf.close();
                    System.exit(0);
                }
                default -> System.out.println("Choix invalide !");
            }
        }
    }

    private static void initialiserDonnees() {
        em.getTransaction().begin();
        long count = em.createQuery("SELECT COUNT(s) FROM Salle s", Long.class).getSingleResult();
        if (count == 0) {
            Categorie informatique = new Categorie("Informatique");
            Categorie conference = new Categorie("Conférence");

            em.persist(informatique);
            em.persist(conference);

            Salle s1 = new Salle("Salle 1", informatique, 30);
            Salle s2 = new Salle("Salle 2", conference, 50);

            em.persist(s1);
            em.persist(s2);
        }

        em.getTransaction().commit();
    }

    private static void listerSalles() {
        List<Salle> salles = em.createQuery("FROM Salle", Salle.class).getResultList();
        if (salles.isEmpty()) {
            System.out.println("Aucune salle trouvée.");
        } else {
            for (Salle salle : salles) {
                System.out.println("- ID: " + salle.getId() + ", Nom: " + salle.getNom() +
                        ", Capacité: " + salle.getCapacite() +
                        ", Catégorie: " + (salle.getCategorie() != null ? salle.getCategorie().getNom() : "Aucune"));
            }
        }
    }

    private static void verifierDisponibilite() {
        System.out.print("Entrez l’ID de la salle : ");
        long idSalle = Long.parseLong(scanner.nextLine());
        Salle salle = em.find(Salle.class, idSalle);

        if (salle == null) {
            System.out.println("Salle introuvable !");
            return;
        }

        List<Reservation> reservations = em.createQuery(
                        "FROM Reservation r WHERE r.salle.id = :idSalle", Reservation.class)
                .setParameter("idSalle", idSalle)
                .getResultList();

        if (reservations.isEmpty()) {
            System.out.println("La salle \"" + salle.getNom() + "\" est disponible.");
        } else {
            System.out.println("La salle \"" + salle.getNom() + "\" a déjà des réservations :");
            for (Reservation r : reservations) {
                System.out.println("- Du " + r.getDateDebut() + " au " + r.getDateFin());
            }
        }
    }

    private static void creerReservation() {
        System.out.print("Nom de l’utilisateur : ");
        String nom = scanner.nextLine();
        System.out.print("Prénom : ");
        String prenom = scanner.nextLine();
        System.out.print("Email : ");
        String email = scanner.nextLine();

        Utilisateur utilisateur = new Utilisateur(nom, prenom, email);

        System.out.print("ID de la salle : ");
        long idSalle = Long.parseLong(scanner.nextLine());
        Salle salle = em.find(Salle.class, idSalle);
        if (salle == null) {
            System.out.println("Salle introuvable !");
            return;
        }

        System.out.print("Date début (format: AAAA-MM-JJ HH:MM) : ");
        String debutStr = scanner.nextLine();
        System.out.print("Date fin (format: AAAA-MM-JJ HH:MM) : ");
        String finStr = scanner.nextLine();

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime dateDebut = LocalDateTime.parse(debutStr, formatter);
            LocalDateTime dateFin = LocalDateTime.parse(finStr, formatter);

            Reservation reservation = new Reservation(dateDebut, dateFin, "Réunion", salle, utilisateur);

            em.getTransaction().begin();
            em.persist(utilisateur);
            em.persist(reservation);
            em.getTransaction().commit();

            System.out.println("Réservation créée avec succès !");
        } catch (Exception e) {
            System.out.println(" Erreur : format de date invalide. Utilisez AAAA-MM-JJ HH:MM");
        }
    }
}
