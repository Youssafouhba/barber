package com.halaq.backend.config;

import com.halaq.backend.core.security.entity.Role;
import com.halaq.backend.core.security.entity.RoleUser;
import com.halaq.backend.core.security.enums.AccountStatus;
import com.halaq.backend.core.security.service.facade.RoleService;
import com.halaq.backend.core.security.service.facade.RoleUserService;
import com.halaq.backend.core.security.service.facade.UserService;
import com.halaq.backend.service.entity.Availability;
import com.halaq.backend.service.entity.OfferedService;
import com.halaq.backend.service.entity.ServiceCategory;
import com.halaq.backend.service.service.facade.ServiceCategoryService;
import com.halaq.backend.shared.DocumentType;
import com.halaq.backend.shared.VerificationStatus;
import com.halaq.backend.user.entity.Barber;
import com.halaq.backend.user.entity.Document;
import com.halaq.backend.user.service.facade.BarberService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Classe exécutée au démarrage de l'application pour initialiser les données essentielles.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final ServiceCategoryService categoryService;
    private final BarberService barberService;
    private final RoleService roleService;
    private final RoleUserService roleUserService;
    private final UserService userService;

    public DataInitializer(ServiceCategoryService categoryService,
                           BarberService barberService,
                           RoleService roleService,
                           RoleUserService roleUserService,
                           UserService userService) {
        this.categoryService = categoryService;
        this.barberService = barberService;
        this.roleService = roleService;
        this.roleUserService = roleUserService;
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        createDefaultServiceCategories();
        createDefaultBarbers();
    }

    private void createDefaultServiceCategories() {
        // Nouvelle structure de données pour associer le nom de la catégorie à son icône/image par défaut.
        // Les URLs sont des placeholders. En production, elles pointeraient vers un CDN (comme AWS S3).
        List<SimpleEntry<String, String>> defaultCategories = Arrays.asList(
                new SimpleEntry<>("Haircuts", "Scissors"),          // Exemple image coupe simple
                new SimpleEntry<>("Beard Trims & Shaves", "Star"),                // Exemple image barbe
                new SimpleEntry<>("Coloring", "Palette"),           // Exemple image coloration
                new SimpleEntry<>("Facial Treatments", "Sparkles")    // Exemple image soin // Exemple image massage
        );


        System.out.println("--- Démarrage de l'initialisation des catégories de services ---");

        for (SimpleEntry<String, String> entry : defaultCategories) {
            String categoryName = entry.getKey();
            String iconUrl = entry.getValue();

            // 1. Vérifier si la catégorie existe déjà
            Optional<ServiceCategory> existingCategory = categoryService.findByName(categoryName);

            if (existingCategory.isEmpty()) {
                // 2. Si elle n'existe pas, la créer
                ServiceCategory newCategory = new ServiceCategory();
                newCategory.setName(categoryName);
                newCategory.setIconUrl(iconUrl); // ASSIGNATION DE L'ICÔNE

                try {
                    categoryService.create(newCategory);
                    System.out.println("Catégorie créée : " + categoryName + " avec icône : " + iconUrl);
                } catch (Exception e) {
                    System.err.println("Erreur lors de la création de la catégorie " + categoryName + ": " + e.getMessage());
                }
            } else {
                // Optionnel: Mettre à jour l'icône si elle a changé dans le code
                if (!existingCategory.get().getIconUrl().equals(iconUrl)) {
                    existingCategory.get().setIconUrl(iconUrl);
                    categoryService.create(existingCategory.get());
                    System.out.println("Icône de la catégorie " + categoryName + " mise à jour.");
                } else {
                    System.out.println("Catégorie existe déjà : " + categoryName);
                }
            }
        }

        System.out.println("--- Initialisation des catégories de services terminée ---");
    }

    private void createDefaultBarbers() {
        System.out.println("--- Démarrage de l'initialisation des barbiers (seed) ---");

        // Ensure ROLE_BARBER exists
        Role barberRole = Optional.ofNullable(roleService.findByAuthority("ROLE_BARBER"))
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setAuthority("ROLE_BARBER");
                    return roleService.create(r);
                });

        // Pull some categories to assign to offered services
        ServiceCategory haircuts = categoryService.findByName("Haircuts").orElse(null);
        ServiceCategory coloring = categoryService.findByName("Coloring").orElse(null);
        ServiceCategory facial = categoryService.findByName("Facial Treatments").orElse(null);

        // Common password hash (bcrypt) for demo users; replace in production
        final String demoPasswordHash = "$2a$10$6k70V8Oi14pYzdn5BAJefOuEZx8USpdEYcvCUzEpTXtpo3vvNLKZ.";

        for (int i = 1; i <= 20; i++) {
            String email = "barber" + i + "@example.com";
            // Skip if already exists
            if (userService.findByEmail(email) != null) {
                System.out.println("Barbier existe déjà: " + email);
                continue;
            }

            Barber b = new Barber();
            b.setEmail(email);
            b.setUsername("Barber " + i);
            b.setPassword(demoPasswordHash);
            b.setFirstName("Barber");
            b.setLastName("#" + i);
            b.setFullName(("BARBER " + i).toUpperCase());
            b.setPhone("+212600000" + String.format("%03d", i));
            b.setAvatar("barber_" + i + "_avatar.jpg");
            b.setAbout("Professional barber with over +" + (2 + (i % 8)) + " years exp");
            b.setCin("CN" + String.format("%06d", 100000 + i));
            b.setAge(22 + (i % 15));
            b.setYearsOfExperience(1 + (i % 12));
            b.setAverageRating(4.0 + (i % 10) * 0.05);
            b.setReviewCount(5 + (i % 40));
            b.setLocation("City-" + ((i % 5) + 1));
            b.setBarberShopName("Shop-" + i);
            b.setStatus(AccountStatus.PENDING_EMAIL_VERIFICATION);
            b.setEnabled(false);

            // Zones
            //b.setZones(Arrays.asList("Zone-" + ((i % 3) + 1), "Zone-" + ((i % 4) + 2)));

            // Portfolio (image names)
            b.setPortfolio(Arrays.asList(
                    "barber_" + i + "_portfolio_1.jpg",
                    "barber_" + i + "_portfolio_2.jpg",
                    "barber_" + i + "_portfolio_3.jpg"
            ));

            // Documents
            List<Document> docs = new ArrayList<>();
            Document cinDoc = new Document();
            cinDoc.setType(DocumentType.CIN);
            cinDoc.setUrl("barber_" + i + "_cin.jpg");
            cinDoc.setName("cin_" + i + ".jpg");
            cinDoc.setSize(25000L);
            cinDoc.setVerificationStatus(VerificationStatus.PENDING);
            cinDoc.setBarber(b);
            docs.add(cinDoc);

            Document diplomaDoc = new Document();
            diplomaDoc.setType(DocumentType.DIPLOMA);
            diplomaDoc.setUrl("barber_" + i + "_diploma.png");
            diplomaDoc.setName("diploma_" + i + ".png");
            diplomaDoc.setSize(150000L);
            diplomaDoc.setVerificationStatus(VerificationStatus.PENDING);
            diplomaDoc.setBarber(b);
            docs.add(diplomaDoc);

            b.setDocuments(docs);

            // Weekly availability (09:00-18:00)
            List<Availability> availability = new ArrayList<>();
            for (DayOfWeek dow : DayOfWeek.values()) {
                Availability av = new Availability();
                av.setDay(dow);
                av.setStartTime(LocalTime.of(9, 0));
                av.setEndTime(LocalTime.of(18, 0));
                av.setBarber(b);
                availability.add(av);
            }
            b.setAvailability(availability);

            // Offered services
            List<OfferedService> offered = new ArrayList<>();
            OfferedService s1 = new OfferedService();
            s1.setName("Basic Cut");
            s1.setDescription("Standard haircut");
            s1.setPrice(new BigDecimal("80.00"));
            s1.setDurationInMinutes(45);
            s1.setCategory(haircuts);
            s1.setBarber(b);
            offered.add(s1);

            OfferedService s2 = new OfferedService();
            s2.setName("Color Boost");
            s2.setDescription("Coloring session");
            s2.setPrice(new BigDecimal("180.00"));
            s2.setDurationInMinutes(60);
            s2.setCategory(coloring);
            s2.setBarber(b);
            offered.add(s2);

            OfferedService s3 = new OfferedService();
            s3.setName("Facial Glow");
            s3.setDescription("Facial treatment");
            s3.setPrice(new BigDecimal("120.00"));
            s3.setDurationInMinutes(40);
            s3.setCategory(facial);
            s3.setBarber(b);
            offered.add(s3);

            b.setOfferedServices(offered);

            // Persist barber with cascades
            Barber created = barberService.create(b);

            // Attach ROLE_BARBER to the created user
            RoleUser ru = new RoleUser(barberRole, created);
            roleUserService.create(ru);

            System.out.println("Barbier créé: " + created.getEmail());
        }

        System.out.println("--- Initialisation des barbiers terminée ---");
    }
}